package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.BlockedUserEntity
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ChannelPostEntity
import com.example.data.local.entity.ConversationMemberEntity
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.GroupPermissionEntity
import com.example.data.local.entity.MessageAttachmentEntity
import com.example.data.local.entity.MessageReactionEntity
import com.example.data.local.entity.MessengerConversationEntity
import com.example.data.local.entity.MessengerMessageEntity
import com.example.data.local.entity.ReportEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

import com.example.data.remote.api.TarhiNooApiService
import com.example.data.remote.config.BackendConfig
import com.example.data.remote.model.SendMessageRequest

/**
 * Clean Room-based repository providing one-shot suspend queries for Tarhi Noo Messenger,
 * covering Users, Conversations, Messages, Groups, Channels, Reactions, Attachments, and Moderation.
 * Adheres strictly to UUID String IDs across all models.
 */
class MessengerRepository(
    private val db: AppDatabase,
    private val apiService: TarhiNooApiService? = null
) {

    // --- Users & Contacts ---
    suspend fun getAllActiveUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        db.messengerUserDao().getAllActiveUsers()
    }

    suspend fun getContacts(): List<UserEntity> = withContext(Dispatchers.IO) {
        db.messengerUserDao().getContacts()
    }

    suspend fun getBlockedUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        db.messengerUserDao().getBlockedUsers()
    }

    suspend fun getUserById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        db.messengerUserDao().getUserById(userId)
    }

    suspend fun getUserByUsername(username: String): UserEntity? = withContext(Dispatchers.IO) {
        val clean = username.removePrefix("@").trim().lowercase()
        db.messengerUserDao().getUserByUsername(clean)
    }

    suspend fun searchUsers(query: String): List<UserEntity> = withContext(Dispatchers.IO) {
        val clean = query.removePrefix("@").trim()
        db.messengerUserDao().searchUsers(clean)
    }

    suspend fun queryRemoteUsers(query: String) = withContext(Dispatchers.IO) {
        if (!BackendConfig.isBackendConfigured || apiService == null) return@withContext
        val clean = query.removePrefix("@").trim()
        if (clean.isBlank()) return@withContext
        try {
            val response = apiService.searchUsers(clean)
            if (response.isSuccessful && response.body()?.success == true) {
                val users = response.body()?.data.orEmpty()
                val entities = users.map { dto ->
                    UserEntity(
                        id = dto.id,
                        username = dto.username,
                        displayName = dto.displayName,
                        avatarUrl = dto.avatarUrl,
                        bio = dto.bio,
                        isOnline = dto.isOnline,
                        isContact = false
                    )
                }
                db.messengerUserDao().insertUsers(entities)
            }
        } catch (_: Exception) {}
    }

    suspend fun insertOrUpdateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        val cleanUser = user.copy(username = user.username.removePrefix("@").trim().lowercase())
        db.messengerUserDao().insertUser(cleanUser)
    }

    suspend fun toggleContact(userId: String, isContact: Boolean) = withContext(Dispatchers.IO) {
        db.messengerUserDao().setUserContact(userId, isContact)
    }

    // --- Conversations ---
    suspend fun getAllConversations(): List<MessengerConversationEntity> = withContext(Dispatchers.IO) {
        db.messengerConversationDao().getAllConversations()
    }

    suspend fun getTotalUnreadCount(): Int? = withContext(Dispatchers.IO) {
        db.messengerConversationDao().getTotalUnreadCount()
    }

    suspend fun getConversationsByType(type: String): List<MessengerConversationEntity> = withContext(Dispatchers.IO) {
        db.messengerConversationDao().getConversationsByType(type)
    }

    suspend fun getArchivedConversations(): List<MessengerConversationEntity> = withContext(Dispatchers.IO) {
        db.messengerConversationDao().getArchivedConversations()
    }

    suspend fun getConversationById(id: String): MessengerConversationEntity? = withContext(Dispatchers.IO) {
        db.messengerConversationDao().getConversationById(id)
    }

    suspend fun getOrCreatePrivateConversation(user: UserEntity, currentUserId: String = "current_user"): String = withContext(Dispatchers.IO) {
        val existing = db.messengerConversationDao().getPrivateConversationWithUser(user.id)
        if (existing != null) {
            existing.id
        } else {
            val convId = UUID.randomUUID().toString()
            val newConv = MessengerConversationEntity(
                id = convId,
                type = "PRIVATE",
                title = user.displayName,
                avatarUrl = user.avatarUrl,
                directUserId = user.id,
                lastMessageText = "گفتگو آغاز شد",
                lastMessageSenderName = user.displayName
            )
            db.messengerConversationDao().insertConversation(newConv)
            // Add conversation members
            db.groupDao().insertMember(
                ConversationMemberEntity(conversationId = convId, userId = currentUserId, role = "MEMBER")
            )
            db.groupDao().insertMember(
                ConversationMemberEntity(conversationId = convId, userId = user.id, role = "MEMBER")
            )
            convId
        }
    }

    suspend fun createGroupConversation(
        name: String,
        description: String,
        selectedUserIds: List<String>,
        ownerId: String = "current_user"
    ): String = withContext(Dispatchers.IO) {
        val convId = UUID.randomUUID().toString()
        val conv = MessengerConversationEntity(
            id = convId,
            type = "GROUP",
            title = name,
            description = description,
            lastMessageText = "گروه ایجاد شد",
            lastMessageSenderName = "سیستم"
        )
        db.messengerConversationDao().insertConversation(conv)
        val groupId = UUID.randomUUID().toString()
        val group = GroupEntity(
            id = groupId,
            conversationId = convId,
            name = name,
            description = description,
            ownerId = ownerId,
            memberCount = selectedUserIds.size + 1,
            inviteCode = "TRH-${System.currentTimeMillis() % 100000}"
        )
        db.groupDao().insertGroup(group)
        // Group permissions
        db.groupDao().insertPermissions(GroupPermissionEntity(id = UUID.randomUUID().toString(), groupId = groupId))
        // Add owner
        db.groupDao().insertMember(
            ConversationMemberEntity(conversationId = convId, userId = ownerId, role = "OWNER")
        )
        // Add selected members
        selectedUserIds.forEach { uid ->
            db.groupDao().insertMember(
                ConversationMemberEntity(conversationId = convId, userId = uid, role = "MEMBER")
            )
        }
        convId
    }

    suspend fun createChannel(
        name: String,
        username: String,
        description: String,
        ownerId: String = "current_user"
    ): String = withContext(Dispatchers.IO) {
        val cleanUsername = username.removePrefix("@").trim().lowercase()
        val convId = UUID.randomUUID().toString()
        val conv = MessengerConversationEntity(
            id = convId,
            type = "CHANNEL",
            title = name,
            description = description,
            lastMessageText = "کانال راه‌اندازی شد",
            lastMessageSenderName = name
        )
        db.messengerConversationDao().insertConversation(conv)
        val channelId = UUID.randomUUID().toString()
        val channel = ChannelEntity(
            id = channelId,
            conversationId = convId,
            name = name,
            username = cleanUsername,
            description = description,
            ownerId = ownerId,
            subscriberCount = 1,
            isSubscribed = true,
            inviteLink = "https://t.me/tarhinoo/$cleanUsername"
        )
        db.channelDao().insertChannel(channel)
        convId
    }

    suspend fun setPinned(convId: String, pinned: Boolean) = withContext(Dispatchers.IO) {
        db.messengerConversationDao().setPinned(convId, pinned)
    }

    suspend fun setMuted(convId: String, muted: Boolean) = withContext(Dispatchers.IO) {
        db.messengerConversationDao().setMuted(convId, muted)
    }

    suspend fun setArchived(convId: String, archived: Boolean) = withContext(Dispatchers.IO) {
        db.messengerConversationDao().setArchived(convId, archived)
    }

    suspend fun markAsRead(convId: String) = withContext(Dispatchers.IO) {
        db.messengerConversationDao().markAsRead(convId)
    }

    suspend fun deleteConversation(convId: String) = withContext(Dispatchers.IO) {
        db.messengerMessageDao().deleteAllMessagesInConversation(convId)
        db.messengerConversationDao().deleteConversationById(convId)
    }

    // --- Messages ---
    suspend fun getMessagesForConversation(convId: String): List<MessengerMessageEntity> = withContext(Dispatchers.IO) {
        db.messengerMessageDao().getMessagesForConversation(convId)
    }

    suspend fun getMessageById(messageId: Long): MessengerMessageEntity? = withContext(Dispatchers.IO) {
        db.messengerMessageDao().getMessageById(messageId)
    }

    suspend fun getPinnedMessages(convId: String): List<MessengerMessageEntity> = withContext(Dispatchers.IO) {
        db.messengerMessageDao().getPinnedMessages(convId)
    }

    suspend fun searchMessagesInConversation(convId: String, query: String): List<MessengerMessageEntity> = withContext(Dispatchers.IO) {
        db.messengerMessageDao().searchMessagesInConversation(convId, query)
    }

    suspend fun globalSearchMessages(query: String): List<MessengerMessageEntity> = withContext(Dispatchers.IO) {
        db.messengerMessageDao().globalSearchMessages(query)
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        senderId: String = "current_user",
        senderDisplayName: String = "من",
        messageType: String = "TEXT",
        replyToMessageId: String? = null,
        replyToText: String? = null,
        replyToSenderName: String? = null,
        forwardedFromMessageId: String? = null,
        forwardedFromSenderName: String? = null,
        isAiGenerated: Boolean = false,
        aiActionPrompt: String? = null,
        aiTargetModule: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val clientRequestId = UUID.randomUUID().toString()
        val isMe = senderId == "current_user" || senderId.isBlank()
        val initialStatus = if (isMe) {
            if (BackendConfig.isBackendConfigured && apiService != null) "PENDING" else "SENT"
        } else "READ"

        val message = MessengerMessageEntity(
            conversationId = conversationId,
            senderId = senderId,
            senderDisplayName = senderDisplayName,
            text = text,
            messageType = messageType,
            deliveryStatus = initialStatus,
            clientRequestId = clientRequestId,
            replyToMessageId = replyToMessageId,
            replyToText = replyToText,
            replyToSenderName = replyToSenderName,
            forwardedFromMessageId = forwardedFromMessageId,
            forwardedFromSenderName = forwardedFromSenderName,
            isAiGenerated = isAiGenerated,
            aiActionPrompt = aiActionPrompt,
            aiTargetModule = aiTargetModule
        )
        val msgId = db.messengerMessageDao().insertMessage(message)
        val unreadDelta = if (!isMe) 1 else 0
        db.messengerConversationDao().updateLastMessage(
            id = conversationId,
            lastText = text.take(60),
            sender = senderDisplayName,
            time = message.createdAt,
            unreadDelta = unreadDelta
        )

        // If backend is active and message is from current user, dispatch immediately to API
        if (isMe && BackendConfig.isBackendConfigured && apiService != null) {
            try {
                val req = SendMessageRequest(
                    clientRequestId = clientRequestId,
                    conversationId = conversationId,
                    text = text,
                    messageType = messageType,
                    replyToMessageId = replyToMessageId,
                    replyToText = replyToText,
                    replyToSenderName = replyToSenderName,
                    aiActionPrompt = aiActionPrompt,
                    aiTargetModule = aiTargetModule
                )
                val response = apiService.sendMessage(conversationId, req)
                if (response.isSuccessful && response.body()?.success == true) {
                    val serverMsg = response.body()!!.data!!
                    db.messengerMessageDao().updateDeliveryStatusAndServerId(
                        id = msgId,
                        status = "SENT",
                        serverId = serverMsg.id
                    )
                } else if (response.code() in 400..499) {
                    db.messengerMessageDao().updateDeliveryStatus(msgId, "FAILED")
                }
            } catch (_: Exception) {
                // Keep status as PENDING, SyncManager will retry when reconnected
            }
        }

        msgId
    }

    suspend fun forwardMessage(
        sourceMessage: MessengerMessageEntity,
        targetConversationId: String,
        senderId: String = "current_user"
    ): Long = withContext(Dispatchers.IO) {
        val forwardMsg = MessengerMessageEntity(
            conversationId = targetConversationId,
            senderId = senderId,
            senderDisplayName = "من",
            text = sourceMessage.text,
            messageType = sourceMessage.messageType,
            deliveryStatus = "SENT",
            forwardedFromMessageId = sourceMessage.serverId ?: sourceMessage.id.toString(),
            forwardedFromSenderName = sourceMessage.senderDisplayName.ifBlank { "کاربر" },
            isAiGenerated = sourceMessage.isAiGenerated,
            aiActionPrompt = sourceMessage.aiActionPrompt,
            aiTargetModule = sourceMessage.aiTargetModule
        )
        val id = db.messengerMessageDao().insertMessage(forwardMsg)
        db.messengerConversationDao().updateLastMessage(
            id = targetConversationId,
            lastText = "پیام هدایت شده: ${sourceMessage.text.take(40)}",
            sender = "من",
            time = forwardMsg.createdAt,
            unreadDelta = 0
        )
        id
    }

    suspend fun editMessage(messageId: Long, newText: String) = withContext(Dispatchers.IO) {
        db.messengerMessageDao().editMessageText(messageId, newText)
    }

    suspend fun setMessagePinned(messageId: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        db.messengerMessageDao().setMessagePinned(messageId, isPinned)
    }

    suspend fun deleteMessage(messageId: Long, forEveryone: Boolean = true) = withContext(Dispatchers.IO) {
        if (forEveryone) {
            db.messengerMessageDao().softDeleteMessage(messageId)
        } else {
            db.messengerMessageDao().deleteMessagePermanently(messageId)
        }
    }

    // --- Reactions ---
    suspend fun getReactionsForMessage(messageId: String): List<MessageReactionEntity> = withContext(Dispatchers.IO) {
        db.reactionDao().getReactionsForMessage(messageId)
    }

    suspend fun toggleReaction(messageId: String, userId: String = "current_user", reactionEmoji: String) = withContext(Dispatchers.IO) {
        val existing = db.reactionDao().getReaction(messageId, userId, reactionEmoji)
        if (existing != null) {
            db.reactionDao().deleteReaction(messageId, userId, reactionEmoji)
        } else {
            db.reactionDao().insertReaction(
                MessageReactionEntity(messageId = messageId, userId = userId, reaction = reactionEmoji)
            )
        }
    }

    // --- Groups & Channels ---
    suspend fun getAllGroups(): List<GroupEntity> = withContext(Dispatchers.IO) {
        db.groupDao().getAllGroups()
    }

    suspend fun getAllChannels(): List<ChannelEntity> = withContext(Dispatchers.IO) {
        db.channelDao().getAllChannels()
    }

    suspend fun getSubscribedChannels(): List<ChannelEntity> = withContext(Dispatchers.IO) {
        db.channelDao().getSubscribedChannels()
    }

    suspend fun getMembersForConversation(convId: String): List<ConversationMemberEntity> = withContext(Dispatchers.IO) {
        db.groupDao().getMembersForConversation(convId)
    }

    suspend fun getGroupByConversationId(convId: String): GroupEntity? = withContext(Dispatchers.IO) {
        db.groupDao().getGroupByConversationId(convId)
    }

    suspend fun getChannelByConversationId(convId: String): ChannelEntity? = withContext(Dispatchers.IO) {
        db.channelDao().getChannelByConversationId(convId)
    }

    suspend fun getPostsForChannel(channelId: String): List<ChannelPostEntity> = withContext(Dispatchers.IO) {
        db.channelDao().getPostsForChannel(channelId)
    }

    suspend fun publishChannelPost(
        channelId: String,
        text: String,
        mediaUrl: String = "",
        mediaType: String = "TEXT",
        promptText: String = "",
        authorId: String = "current_user"
    ): String = withContext(Dispatchers.IO) {
        val postId = UUID.randomUUID().toString()
        db.channelDao().insertPost(
            ChannelPostEntity(
                id = postId,
                channelId = channelId,
                authorId = authorId,
                authorName = "طرحی نو مدیا",
                text = text,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                promptText = promptText
            )
        )
        postId
    }

    suspend fun toggleChannelSubscription(channelId: String, isSubscribed: Boolean) = withContext(Dispatchers.IO) {
        db.channelDao().setSubscribed(channelId, isSubscribed)
    }

    // --- Moderation & Reporting ---
    suspend fun blockUser(userId: String, reason: String = "") = withContext(Dispatchers.IO) {
        db.messengerUserDao().setUserBlocked(userId, true)
        db.moderationDao().insertBlockedUser(BlockedUserEntity(userId = userId, reason = reason))
    }

    suspend fun unblockUser(userId: String) = withContext(Dispatchers.IO) {
        db.messengerUserDao().setUserBlocked(userId, false)
        db.moderationDao().removeBlockedUser(userId)
    }

    suspend fun report(targetType: String, targetId: String, reason: String, details: String = "", reportedByUserId: String = "current_user"): Long = withContext(Dispatchers.IO) {
        db.moderationDao().insertReport(
            ReportEntity(
                targetType = targetType,
                targetId = targetId,
                reasonCategory = reason,
                details = details,
                reportedByUserId = reportedByUserId
            )
        )
    }

    // --- Initial Seed Data for Messenger (Real, clean local seed with UUID Strings) ---
    suspend fun seedInitialMessengerDataIfNeeded() = withContext(Dispatchers.IO) {
        if (db.messengerUserDao().getUserCount() == 0) {
            val isDebug = try {
                com.example.BuildConfig.DEBUG
            } catch (_: Exception) {
                false
            }

            // Always seed Tarhi Noo AI
            val aiUserId = "tarhinoo_ai"
            val aiConvId = "conv_tarhinoo_ai"
            val aiUser = UserEntity(
                id = aiUserId,
                username = "tarhinoo_ai",
                displayName = "هوش مصنوعی طرحی نو",
                bio = "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.» معمار پرامپت، مشاور هنری و پردازش خلاق",
                isOnline = true,
                isVerified = true,
                isContact = true
            )
            db.messengerUserDao().insertUser(aiUser)

            val aiConv = MessengerConversationEntity(
                id = aiConvId,
                type = "AI",
                title = "هوش مصنوعی طرحی نو (@TarhiNooAI)",
                avatarUrl = "",
                directUserId = aiUserId,
                unreadCount = 1,
                lastMessageText = "درود! آماده ساخت پرامپت، پوستر یا سناریوی خلاق هستم.",
                lastMessageSenderName = "هوش مصنوعی طرحی نو"
            )
            db.messengerConversationDao().insertConversation(aiConv)
            db.messengerMessageDao().insertMessage(
                MessengerMessageEntity(
                    conversationId = aiConvId,
                    senderId = aiUserId,
                    senderDisplayName = "هوش مصنوعی طرحی نو",
                    text = "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\nمی‌توانید مستقیماً در این چت با من گفتگو کنید، یا در هر گروه و کانال با منشن @TarhiNooAI مشاوره پرامپت دریافت کنید.",
                    messageType = "AI_RESULT",
                    isAiGenerated = true,
                    aiActionPrompt = "Cinematic Iranian architectural pavilion at golden hour, traditional turquoise tiles with volumetric sunlight, 8K render",
                    aiTargetModule = "PROMPT_BUILDER"
                )
            )

            // Only seed mock users and demo conversations in DEBUG mode
            if (isDebug) {
                val userRezaId = "user_reza_mahdavi"
                val userSaraId = "user_sara_alavi"
                val userStudioId = "user_tarhineh_studio"

                val demoUsers = listOf(
                    UserEntity(
                        id = userRezaId,
                        username = "reza_art",
                        displayName = "رضا مهدوی",
                        bio = "طراح هویت بصری و کاربر حرفه‌ای معمار پرامپت طرحی نو",
                        isOnline = true,
                        isVerified = true,
                        isContact = true
                    ),
                    UserEntity(
                        id = userSaraId,
                        username = "sara_cinema",
                        displayName = "سارا علوی",
                        bio = "فیلمساز و کارگردان هوش مصنوعی - تخصص در ویدیوهای سینمایی Veo و Sora",
                        isOnline = false,
                        isVerified = false,
                        isContact = true
                    ),
                    UserEntity(
                        id = userStudioId,
                        username = "tarhineh_studio",
                        displayName = "استودیو طرحینه",
                        bio = "کانون نوآوری هنر رسانه‌ای طرحینه مدیا",
                        isOnline = true,
                        isVerified = true,
                        isContact = false
                    )
                )
                db.messengerUserDao().insertUsers(demoUsers)

                // 2. Private chat with Reza Mahdavi
                val convRezaId = "conv_reza_mahdavi"
                val rezaConv = MessengerConversationEntity(
                    id = convRezaId,
                    type = "PRIVATE",
                    title = "رضا مهدوی",
                    avatarUrl = "",
                    directUserId = userRezaId,
                    unreadCount = 0,
                    lastMessageText = "سلام علی جان، پرامپت پوستر محرم رو در ناو استودیو تست کردی؟",
                    lastMessageSenderName = "رضا مهدوی"
                )
                db.messengerConversationDao().insertConversation(rezaConv)
                db.messengerMessageDao().insertMessage(
                    MessengerMessageEntity(
                        conversationId = convRezaId,
                        senderId = userRezaId,
                        senderDisplayName = "رضا مهدوی",
                        text = "سلام علی جان، پرامپت پوستر محرم رو در ناو استودیو تست کردی؟ کیفیت نورپردازی حجمی فوق‌العاده شده بود.",
                        messageType = "TEXT"
                    )
                )

                // 3. Creative Group
                val convGroupId = "conv_group_designers"
                val groupConv = MessengerConversationEntity(
                    id = convGroupId,
                    type = "GROUP",
                    title = "انجمن طراحان هوش مصنوعی طرحینه",
                    avatarUrl = "",
                    unreadCount = 2,
                    lastMessageText = "سارا علوی: جدیدترین مقایسه فلوکس و میدجرنی رو گذاشتم",
                    lastMessageSenderName = "سارا علوی"
                )
                db.messengerConversationDao().insertConversation(groupConv)
                val groupId = "group_designers"
                val group = GroupEntity(
                    id = groupId,
                    conversationId = convGroupId,
                    name = "انجمن طراحان هوش مصنوعی طرحینه",
                    description = "گفتگوی تخصصی حول معمار پرامپت، ناو استودیو، موشن و نوآوری‌های هنر دیجیتال",
                    ownerId = "system_owner",
                    memberCount = 38,
                    inviteCode = "TRH-GRP-2026"
                )
                db.groupDao().insertGroup(group)
                db.groupDao().insertPermissions(GroupPermissionEntity(id = "perm_group_designers", groupId = groupId))
                db.messengerMessageDao().insertMessage(
                    MessengerMessageEntity(
                        conversationId = convGroupId,
                        senderId = userSaraId,
                        senderDisplayName = "سارا علوی",
                        text = "دوستان عزیز، با پرامپت معمار طرحی نو، نتیجه ویدیوی 60fps با دوربین Dolly Zoom فوق‌العاده نرم شد!",
                        messageType = "TEXT"
                    )
                )

                // 4. Official Channel
                val convChannelId = "conv_channel_tarhineh"
                val channelConv = MessengerConversationEntity(
                    id = convChannelId,
                    type = "CHANNEL",
                    title = "رسانه هنری طرحینه مدیا",
                    avatarUrl = "",
                    unreadCount = 0,
                    lastMessageText = "انتشار نسخه جدید معمار پرامپت طرحی نو با پشتیبانی کامل ناو استودیو",
                    lastMessageSenderName = "طرحینه مدیا"
                )
                db.messengerConversationDao().insertConversation(channelConv)
                val channelId = "channel_tarhineh_media"
                val channel = ChannelEntity(
                    id = channelId,
                    conversationId = convChannelId,
                    name = "رسانه هنری طرحینه مدیا",
                    username = "tarhineh_media",
                    description = "کانال رسمی اطلاع‌رسانی، انتشار الگوهای خلاق و تکنیک‌های برتر هوش مصنوعی",
                    ownerId = "system_owner",
                    subscriberCount = 1420,
                    isSubscribed = true,
                    inviteLink = "https://tarhineh.media/c/tarhineh_media"
                )
                db.channelDao().insertChannel(channel)
                db.channelDao().insertPost(
                    ChannelPostEntity(
                        id = "post_channel_tarhineh_1",
                        channelId = channelId,
                        authorId = "tarhineh_media",
                        authorName = "طرحینه مدیا",
                        text = "«طرحی نو — رسانه هنری طرحینه مدیا»\n\nپیام‌رسان مستقل و یکپارچه طرحی نو راه‌اندازی شد. اکنون می‌توانید پروژه‌ها، پرامپت‌ها و لایه‌های ناو استودیو را مستقیماً با هنرمندان به اشتراک بگذارید.",
                        promptText = "Futuristic neon Persian calligraphy floating over obsidian water, emerald glow, 8k luxury style",
                        reactionCount = 89,
                        viewCount = 650
                    )
                )
            }
        }
    }
}
