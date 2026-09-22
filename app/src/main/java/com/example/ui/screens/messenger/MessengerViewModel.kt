package com.example.ui.screens.messenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ChannelPostEntity
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.MessengerConversationEntity
import com.example.data.local.entity.MessengerMessageEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.MessengerRepository
import com.example.data.remote.realtime.RealtimeEvent
import com.example.data.remote.realtime.RealtimeManager
import com.example.data.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessengerViewModel(
    private val repository: MessengerRepository,
    private val realtimeManager: RealtimeManager? = null,
    private val syncManager: SyncManager? = null,
    private val geminiService: GeminiService = GeminiService()
) : ViewModel() {

    private val _isUserTyping = MutableStateFlow(false)
    val isUserTyping: StateFlow<Boolean> = _isUserTyping.asStateFlow()

    private val _conversations = MutableStateFlow<List<MessengerConversationEntity>>(emptyList())
    val conversations: StateFlow<List<MessengerConversationEntity>> = _conversations.asStateFlow()

    private val _groups = MutableStateFlow<List<GroupEntity>>(emptyList())
    val groups: StateFlow<List<GroupEntity>> = _groups.asStateFlow()

    private val _channels = MutableStateFlow<List<ChannelEntity>>(emptyList())
    val channels: StateFlow<List<ChannelEntity>> = _channels.asStateFlow()

    private val _contacts = MutableStateFlow<List<UserEntity>>(emptyList())
    val contacts: StateFlow<List<UserEntity>> = _contacts.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserEntity>>(emptyList())
    val allUsers: StateFlow<List<UserEntity>> = _allUsers.asStateFlow()

    // Active conversation state
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

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

    init {
        viewModelScope.launch {
            repository.seedInitialMessengerDataIfNeeded()
            refreshAll()
        }

        // Listen for realtime events if manager available
        realtimeManager?.let { rm ->
            viewModelScope.launch {
                rm.events.collect { event ->
                    when (event) {
                        is RealtimeEvent.UserTyping -> {
                            val currentId = _currentConversationId.value
                            if (currentId != null && event.conversationId == currentId) {
                                _isUserTyping.value = event.isTyping
                            }
                        }
                        is RealtimeEvent.MessageCreated,
                        is RealtimeEvent.MessageUpdated,
                        is RealtimeEvent.MessageDeleted -> {
                            refreshAll()
                            _currentConversationId.value?.let { convId ->
                                refreshMessages(convId)
                            }
                        }
                        else -> {
                            refreshAll()
                        }
                    }
                }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _conversations.value = repository.getAllConversations()
            _groups.value = repository.getAllGroups()
            _channels.value = repository.getAllChannels()
            _contacts.value = repository.getContacts()
            _allUsers.value = repository.getAllActiveUsers()
        }
    }

    private suspend fun refreshMessages(convId: String) {
        _activeMessages.value = repository.getMessagesForConversation(convId)
    }

    fun selectConversation(conversationId: String) {
        _currentConversationId.value = conversationId
        viewModelScope.launch {
            repository.markAsRead(conversationId)
            _activeConversation.value = repository.getConversationById(conversationId)
            refreshMessages(conversationId)
            refreshAll()
        }
    }

    fun selectChannel(conversationId: String) {
        viewModelScope.launch {
            val channel = repository.getChannelByConversationId(conversationId)
            _activeChannel.value = channel
            if (channel != null) {
                _activeChannelPosts.value = repository.getPostsForChannel(channel.id)
            }
        }
    }

    fun selectUserProfile(userId: String) {
        viewModelScope.launch {
            _activeUserProfile.value = repository.getUserById(userId)
        }
    }

    fun startPrivateChatWithUser(user: UserEntity, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val convId = repository.getOrCreatePrivateConversation(user)
            selectConversation(convId)
            refreshAll()
            onReady(convId)
        }
    }

    fun startAIChatConversation(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val aiUser = repository.getUserByUsername("tarhinoo_ai") ?: UserEntity(
                id = "tarhinoo_ai",
                username = "tarhinoo_ai",
                displayName = "هوش مصنوعی طرحی نو",
                bio = "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»",
                isOnline = true,
                isVerified = true,
                isContact = true
            )
            val convId = repository.getOrCreatePrivateConversation(aiUser)
            selectConversation(convId)
            refreshAll()
            onReady(convId)
        }
    }

    fun createGroup(name: String, desc: String, members: List<String>, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val convId = repository.createGroupConversation(name, desc, members)
            selectConversation(convId)
            refreshAll()
            onCreated(convId)
        }
    }

    fun createChannel(name: String, username: String, desc: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val convId = repository.createChannel(name, username, desc)
            selectChannel(convId)
            refreshAll()
            onCreated(convId)
        }
    }

    fun sendMessage(
        text: String,
        replyToId: String? = null,
        replyToText: String? = null,
        replyToSender: String? = null
    ) {
        val convId = _currentConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = text,
                senderId = "current_user",
                senderDisplayName = "من",
                replyToMessageId = replyToId,
                replyToText = replyToText,
                replyToSenderName = replyToSender
            )
            refreshMessages(convId)
            refreshAll()

            // AI Mention or AI Conversation: Real Creative AI Intelligence
            val conv = _activeConversation.value
            if (conv?.type == "AI" || text.contains("@TarhiNooAI", ignoreCase = true)) {
                val cleanPrompt = text.removePrefix("@TarhiNooAI").trim()
                val targetModule = if (cleanPrompt.contains("موسیقی") || cleanPrompt.contains("صوت") || cleanPrompt.contains("audio") || cleanPrompt.contains("music")) {
                    "NAVA_STUDIO"
                } else {
                    "PROMPT_BUILDER"
                }
                val realAiResponse = geminiService.generateCreativeResponse(
                    userPrompt = if (cleanPrompt.isNotBlank()) cleanPrompt else "معماری سینمایی ایرانی و هنر مفهومی",
                    systemInstruction = "You are Tarhi Noo AI (هوش مصنوعی طرحی نو), from Tarhineh Media (رسانه هنری طرحینه مدیا). Always introduce yourself as: «من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.» Provide visionary artistic, prompt engineering, and cinematic concepts in fluent Persian."
                )

                repository.sendMessage(
                    conversationId = convId,
                    text = realAiResponse,
                    senderId = "tarhinoo_ai",
                    senderDisplayName = "هوش مصنوعی طرحی نو",
                    messageType = "AI_RESULT",
                    isAiGenerated = true,
                    aiActionPrompt = if (cleanPrompt.isNotBlank()) cleanPrompt else "Cinematic Iranian architecture at twilight with turquoise glowing tiles, volumetric lighting, 8k luxury",
                    aiTargetModule = targetModule
                )
                refreshMessages(convId)
                refreshAll()
            }
        }
    }

    fun forwardMessage(source: MessengerMessageEntity, targetConvId: String) {
        viewModelScope.launch {
            repository.forwardMessage(source, targetConvId)
            refreshAll()
        }
    }

    fun editMessage(messageId: Long, newText: String) {
        viewModelScope.launch {
            repository.editMessage(messageId, newText)
            _currentConversationId.value?.let { refreshMessages(it) }
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
            _currentConversationId.value?.let { refreshMessages(it) }
        }
    }

    fun togglePinMessage(messageId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.setMessagePinned(messageId, isPinned)
            _currentConversationId.value?.let { refreshMessages(it) }
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(messageId = messageId, reactionEmoji = emoji)
            _currentConversationId.value?.let { refreshMessages(it) }
        }
    }

    fun togglePinConversation(convId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.setPinned(convId, isPinned)
            refreshAll()
        }
    }

    fun toggleMuteConversation(convId: String, isMuted: Boolean) {
        viewModelScope.launch {
            repository.setMuted(convId, isMuted)
            refreshAll()
        }
    }

    fun toggleArchiveConversation(convId: String, isArchived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(convId, isArchived)
            refreshAll()
        }
    }

    fun deleteConversation(convId: String) {
        viewModelScope.launch {
            repository.deleteConversation(convId)
            refreshAll()
        }
    }

    fun markAsRead(convId: String) {
        viewModelScope.launch {
            repository.markAsRead(convId)
            refreshAll()
        }
    }

    fun toggleChannelSubscription(channelId: String, isSubscribed: Boolean) {
        viewModelScope.launch {
            repository.toggleChannelSubscription(channelId, isSubscribed)
            _activeChannel.value = _activeChannel.value?.copy(isSubscribed = isSubscribed)
            refreshAll()
        }
    }

    fun publishChannelPost(channelId: String, text: String, promptText: String) {
        viewModelScope.launch {
            repository.publishChannelPost(channelId, text, promptText = promptText)
            _activeChannelPosts.value = repository.getPostsForChannel(channelId)
            refreshAll()
        }
    }

    fun toggleBlockUser(userId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            if (isBlocked) {
                repository.blockUser(userId)
            } else {
                repository.unblockUser(userId)
            }
            _activeUserProfile.value = _activeUserProfile.value?.copy(isBlocked = isBlocked)
            refreshAll()
        }
    }

    fun report(targetType: String, targetId: String, reason: String) {
        viewModelScope.launch {
            repository.report(targetType, targetId, reason)
        }
    }
}

class MessengerViewModelFactory(
    private val repository: MessengerRepository,
    private val realtimeManager: RealtimeManager? = null,
    private val syncManager: SyncManager? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessengerViewModel::class.java)) {
            return MessengerViewModel(repository, realtimeManager, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
