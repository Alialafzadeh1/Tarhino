package com.example.data.sync

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ChannelPostEntity
import com.example.data.local.entity.ConversationMemberEntity
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.GroupPermissionEntity
import com.example.data.local.entity.MessageReactionEntity
import com.example.data.local.entity.MessengerConversationEntity
import com.example.data.local.entity.MessengerMessageEntity
import com.example.data.local.entity.UserEntity
import com.example.data.remote.api.TarhiNooApiService
import com.example.data.remote.auth.AuthState
import com.example.data.remote.auth.SessionManager
import com.example.data.remote.config.BackendConfig
import com.example.data.remote.config.BackendMode
import com.example.data.remote.model.SendMessageRequest
import com.example.data.remote.realtime.RealtimeEvent
import com.example.data.remote.realtime.RealtimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SyncManager coordinates:
 * - Realtime events from RealtimeManager -> updates Room DB
 * - Room DB updates -> triggers UI observation
 * - Retries pending offline messages when backend reconnects
 * - Synchronizes conversations, groups, channels, and unread receipts
 */
class SyncManager(
    private val db: AppDatabase,
    private val apiService: TarhiNooApiService?,
    private val realtimeManager: RealtimeManager,
    private val sessionManager: SessionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncState = MutableStateFlow<String>("آماده (محلی)")
    val syncState: StateFlow<String> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    init {
        observeAuthState()
        observeRealtimeEvents()
    }

    private fun observeAuthState() {
        scope.launch {
            sessionManager.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        _syncState.value = "متصل به حساب کاربری: ${state.user.displayName}"
                        realtimeManager.connect(state.token, state.user.id)
                        performInitialSync()
                        retryPendingMessages()
                    }
                    is AuthState.NoSession -> {
                        _syncState.value = "حالت محلی (آفلاین)"
                        realtimeManager.disconnect()
                    }
                    is AuthState.SessionExpired -> {
                        _syncState.value = "نشست منقضی شده است"
                        realtimeManager.disconnect()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeRealtimeEvents() {
        scope.launch {
            realtimeManager.events.collect { event ->
                handleRealtimeEvent(event)
            }
        }
    }

    private suspend fun handleRealtimeEvent(event: RealtimeEvent) = withContext(Dispatchers.IO) {
        when (event) {
            is RealtimeEvent.MessageCreated -> {
                val convId = event.conversationId
                if (convId.isBlank()) return@withContext
                val senderId = event.senderId

                // Deduplication check via clientRequestId or serverId
                val existingByReqId = if (!event.clientRequestId.isNullOrBlank()) {
                    db.messengerMessageDao().getMessageByClientRequestId(event.clientRequestId)
                } else null

                val existingByServerId = db.messengerMessageDao().getMessageByServerId(event.messageId)

                if (existingByReqId != null) {
                    db.messengerMessageDao().updateDeliveryStatusAndServerId(
                        id = existingByReqId.id,
                        status = "DELIVERED",
                        serverId = event.messageId
                    )
                } else if (existingByServerId == null) {
                    val entity = MessengerMessageEntity(
                        serverId = event.messageId,
                        clientRequestId = event.clientRequestId,
                        conversationId = convId,
                        senderId = senderId,
                        senderDisplayName = event.senderDisplayName,
                        text = event.text,
                        messageType = event.messageType,
                        deliveryStatus = "DELIVERED",
                        replyToMessageId = event.replyToId,
                        aiActionPrompt = event.aiActionPrompt,
                        aiTargetModule = event.aiTargetModule,
                        createdAt = event.createdAt
                    )
                    db.messengerMessageDao().insertMessage(entity)

                    // Update conversation last message & unread count
                    val currentUserId = sessionManager.getCurrentUserId().orEmpty()
                    val unreadDelta = if (senderId != currentUserId) 1 else 0
                    db.messengerConversationDao().updateLastMessage(
                        id = convId,
                        lastText = event.text,
                        sender = event.senderDisplayName,
                        time = event.createdAt,
                        unreadDelta = unreadDelta
                    )
                }
            }
            is RealtimeEvent.MessageUpdated -> {
                db.messengerMessageDao().editMessageByServerId(event.messageId, event.newText, event.editedAt)
            }
            is RealtimeEvent.MessageDeleted -> {
                db.messengerMessageDao().softDeleteMessageByServerId(event.messageId)
            }
            is RealtimeEvent.MessageRead -> {
                if (event.conversationId.isNotBlank()) {
                    db.messengerConversationDao().markAsRead(event.conversationId)
                }
            }
            is RealtimeEvent.ReactionAdded -> {
                db.reactionDao().insertReaction(
                    MessageReactionEntity(
                        messageId = event.messageId,
                        userId = event.userId,
                        reaction = event.reaction
                    )
                )
            }
            is RealtimeEvent.ReactionRemoved -> {
                db.reactionDao().deleteReaction(event.messageId, event.userId, event.reaction)
            }
            is RealtimeEvent.ChannelPostPublished -> {
                val post = ChannelPostEntity(
                    id = event.postId,
                    channelId = event.channelId,
                    authorId = "",
                    authorName = event.authorName,
                    text = event.text,
                    mediaUrl = event.mediaUrl,
                    promptText = event.promptText,
                    createdAt = event.createdAt
                )
                db.channelDao().insertPost(post)
            }
            is RealtimeEvent.UserPresence -> {
                if (event.userId.isNotBlank()) {
                    db.messengerUserDao().setUserOnlineStatus(event.userId, event.isOnline, event.lastSeen)
                }
            }
            is RealtimeEvent.UserTyping -> {
                // In-memory or volatile typing state can be observed by ViewModel
            }
        }
    }

    /**
     * Initial sync pulls latest conversations from the backend and updates Room cache.
     */
    suspend fun performInitialSync() = withContext(Dispatchers.IO) {
        if (apiService == null || !BackendConfig.isBackendConfigured) {
            _syncState.value = "همگام‌سازی محلی (دیتابیس در دسترس است)"
            return@withContext
        }

        try {
            _syncState.value = "در حال دریافت داده‌های سرور..."
            val response = apiService.getConversations()
            if (response.isSuccessful && response.body()?.success == true) {
                val conversations = response.body()?.data.orEmpty()
                conversations.forEach { dto ->
                    if (dto.id.isNotBlank()) {
                        val entity = MessengerConversationEntity(
                            id = dto.id,
                            type = dto.type,
                            title = dto.title,
                            avatarUrl = dto.avatarUrl,
                            description = dto.description,
                            directUserId = dto.directUserId,
                            unreadCount = dto.unreadCount,
                            lastMessageText = dto.lastMessageText,
                            lastMessageSenderName = dto.lastMessageSenderName,
                            lastMessageTimestamp = dto.lastMessageTimestamp,
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt
                        )
                        db.messengerConversationDao().insertConversation(entity)
                    }
                }
                _lastSyncTime.value = System.currentTimeMillis()
                _syncState.value = "همگام‌سازی کامل با سرور انجام شد"
            } else {
                _syncState.value = "خطا در برقراری ارتباط با سرور"
            }
        } catch (e: Exception) {
            Log.w("TarhiNooSync", "Initial sync network error: ${e.message}")
            _syncState.value = "عدم دسترسی به سرور — استفاده از حافظه محلی"
        }
    }

    /**
     * Retries sending any messages that were saved with status "PENDING" while offline.
     */
    suspend fun retryPendingMessages() = withContext(Dispatchers.IO) {
        if (apiService == null || !BackendConfig.isBackendConfigured) return@withContext

        try {
            val pendingMessages = db.messengerMessageDao().getPendingMessages()
            if (pendingMessages.isEmpty()) return@withContext

            Log.d("TarhiNooSync", "Retrying ${pendingMessages.size} pending messages...")
            for (msg in pendingMessages) {
                val clientReqId = msg.clientRequestId ?: java.util.UUID.randomUUID().toString()
                val request = com.example.data.remote.model.SendMessageRequest(
                    clientRequestId = clientReqId,
                    conversationId = msg.conversationId,
                    text = msg.text,
                    messageType = msg.messageType,
                    replyToMessageId = msg.replyToMessageId,
                    replyToText = msg.replyToText,
                    replyToSenderName = msg.replyToSenderName,
                    aiActionPrompt = msg.aiActionPrompt,
                    aiTargetModule = msg.aiTargetModule
                )
                try {
                    val response = apiService.sendMessage(msg.conversationId, request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverMsg = response.body()!!.data!!
                        db.messengerMessageDao().updateDeliveryStatusAndServerId(
                            id = msg.id,
                            status = "SENT",
                            serverId = serverMsg.id
                        )
                    } else if (response.code() in 400..499) {
                        // Permanent failure from backend
                        db.messengerMessageDao().updateDeliveryStatus(msg.id, "FAILED")
                    }
                } catch (networkEx: Exception) {
                    Log.w("TarhiNooSync", "Network error retrying message ${msg.id}: ${networkEx.message}")
                    break
                }
            }
        } catch (e: Exception) {
            Log.e("TarhiNooSync", "Error in retryPendingMessages: ${e.message}")
        }
    }
}
