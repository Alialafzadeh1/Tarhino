package com.example.ui.screens.messenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ChannelPostEntity
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.MessengerConversationEntity
import com.example.data.local.entity.MessengerMessageEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.MessengerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessengerViewModel(
    private val repository: MessengerRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.seedInitialMessengerDataIfNeeded()
        }
    }

    val conversations: StateFlow<List<MessengerConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<GroupEntity>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<ChannelEntity>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<UserEntity>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allActiveUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active conversation state
    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId: StateFlow<Long?> = _currentConversationId.asStateFlow()

    private val _activeConversation = MutableStateFlow<MessengerConversationEntity?>(null)
    val activeConversation: StateFlow<MessengerConversationEntity?> = _activeConversation.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<MessengerMessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<MessengerMessageEntity>> = _activeMessages.asStateFlow()

    // Active channel state
    private val _activeChannel = MutableStateFlow<ChannelEntity?>(null)
    val activeChannel: StateFlow<ChannelEntity?> = _activeChannel.asStateFlow()

    private val _activeChannelPosts = MutableStateFlow<List<ChannelPostEntity>>(emptyList())
    val activeChannelPosts: StateFlow<List<ChannelPostEntity>> = _activeChannelPosts.asStateFlow()

    // Active user profile state
    private val _activeUserProfile = MutableStateFlow<UserEntity?>(null)
    val activeUserProfile: StateFlow<UserEntity?> = _activeUserProfile.asStateFlow()

    fun selectConversation(conversationId: Long) {
        _currentConversationId.value = conversationId
        viewModelScope.launch {
            repository.markAsRead(conversationId)
            _activeConversation.value = repository.getConversationById(conversationId)
            repository.getMessagesForConversation(conversationId).collect { msgList ->
                _activeMessages.value = msgList
            }
        }
    }

    fun selectChannel(conversationId: Long) {
        viewModelScope.launch {
            val channel = repository.getChannelByConversationId(conversationId)
            _activeChannel.value = channel
            if (channel != null) {
                repository.getPostsForChannel(channel.id).collect { pList ->
                    _activeChannelPosts.value = pList
                }
            }
        }
    }

    fun selectUserProfile(userId: Long) {
        viewModelScope.launch {
            _activeUserProfile.value = repository.getUserById(userId)
        }
    }

    fun startPrivateChatWithUser(user: UserEntity, onReady: (Long) -> Unit) {
        viewModelScope.launch {
            val convId = repository.getOrCreatePrivateConversation(user)
            selectConversation(convId)
            onReady(convId)
        }
    }

    fun startAIChatConversation(onReady: (Long) -> Unit) {
        viewModelScope.launch {
            // Find existing AI conv or create
            val aiUser = repository.getUserByUsername("tarhinoo_ai") ?: UserEntity(
                id = 1L,
                username = "tarhinoo_ai",
                displayName = "هوش مصنوعی طرحی نو",
                bio = "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»",
                isOnline = true,
                isVerified = true,
                isContact = true
            )
            val convId = repository.getOrCreatePrivateConversation(aiUser)
            selectConversation(convId)
            onReady(convId)
        }
    }

    fun createGroup(name: String, desc: String, members: List<Long>, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val convId = repository.createGroupConversation(name, desc, members)
            selectConversation(convId)
            onCreated(convId)
        }
    }

    fun createChannel(name: String, username: String, desc: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val convId = repository.createChannel(name, username, desc)
            selectChannel(convId)
            onCreated(convId)
        }
    }

    fun sendMessage(
        text: String,
        replyToId: Long? = null,
        replyToText: String? = null,
        replyToSender: String? = null
    ) {
        val convId = _currentConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = text,
                senderId = 0L,
                senderDisplayName = "من",
                replyToMessageId = replyToId,
                replyToText = replyToText,
                replyToSenderName = replyToSender
            )

            // AI Mention / AI Conversation Simulation trigger
            val conv = _activeConversation.value
            if (conv?.type == "AI" || text.contains("@TarhiNooAI", ignoreCase = true)) {
                // Simulate smart assistant response from Tarhi Noo AI
                val cleanPrompt = text.removePrefix("@TarhiNooAI").trim()
                val aiResponseText = "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\nدرخواست شما دریافت شد: «$cleanPrompt»\n\nبرای این موضوع، پرامپت معماری و سینمایی با نورپردازی حجمی، نسبت ابعاد 16:9 و رندر 8K آماده شد. با لمس دکمه زیر می‌توانید آن را مستقیماً به Prompt Builder انتقال دهید."
                repository.sendMessage(
                    conversationId = convId,
                    text = aiResponseText,
                    senderId = 1L,
                    senderDisplayName = "هوش مصنوعی طرحی نو",
                    messageType = "AI_RESULT",
                    isAiGenerated = true,
                    aiActionPrompt = if (cleanPrompt.isNotBlank()) cleanPrompt else "Cinematic Iranian architecture at twilight with turquoise glowing tiles, 8k luxury",
                    aiTargetModule = "PROMPT_BUILDER"
                )
            }
        }
    }

    fun forwardMessage(source: MessengerMessageEntity, targetConvId: Long) {
        viewModelScope.launch {
            repository.forwardMessage(source, targetConvId)
        }
    }

    fun editMessage(messageId: Long, newText: String) {
        viewModelScope.launch {
            repository.editMessage(messageId, newText)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun togglePinMessage(messageId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.setMessagePinned(messageId, isPinned)
        }
    }

    fun toggleReaction(messageId: Long, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(messageId = messageId, reactionEmoji = emoji)
        }
    }

    fun togglePinConversation(convId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.setPinned(convId, isPinned)
        }
    }

    fun toggleMuteConversation(convId: Long, isMuted: Boolean) {
        viewModelScope.launch {
            repository.setMuted(convId, isMuted)
        }
    }

    fun toggleArchiveConversation(convId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(convId, isArchived)
        }
    }

    fun deleteConversation(convId: Long) {
        viewModelScope.launch {
            repository.deleteConversation(convId)
        }
    }

    fun markAsRead(convId: Long) {
        viewModelScope.launch {
            repository.markAsRead(convId)
        }
    }

    fun toggleChannelSubscription(channelId: Long, isSubscribed: Boolean) {
        viewModelScope.launch {
            repository.toggleChannelSubscription(channelId, isSubscribed)
            _activeChannel.value = _activeChannel.value?.copy(isSubscribed = isSubscribed)
        }
    }

    fun publishChannelPost(channelId: Long, text: String, promptText: String) {
        viewModelScope.launch {
            repository.publishChannelPost(channelId, text, promptText = promptText)
        }
    }

    fun toggleBlockUser(userId: Long, isBlocked: Boolean) {
        viewModelScope.launch {
            if (isBlocked) {
                repository.blockUser(userId)
            } else {
                repository.unblockUser(userId)
            }
            _activeUserProfile.value = _activeUserProfile.value?.copy(isBlocked = isBlocked)
        }
    }

    fun report(targetType: String, targetId: Long, reason: String) {
        viewModelScope.launch {
            repository.report(targetType, targetId, reason)
        }
    }
}

class MessengerViewModelFactory(
    private val repository: MessengerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessengerViewModel::class.java)) {
            return MessengerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
