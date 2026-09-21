package com.example.data.remote.realtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * State of the Realtime connection.
 * As required by Phase 9 instructions:
 * CONNECTING, CONNECTED, DISCONNECTED, RECONNECTING, FAILED.
 */
enum class RealtimeConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED
}

sealed class RealtimeEvent {
    // Messages
    data class MessageCreated(
        val conversationId: String,
        val messageId: String,
        val senderId: String,
        val senderDisplayName: String,
        val text: String,
        val messageType: String,
        val clientRequestId: String?,
        val replyToId: String?,
        val aiActionPrompt: String?,
        val aiTargetModule: String?,
        val createdAt: Long
    ) : RealtimeEvent()

    data class MessageUpdated(
        val conversationId: String,
        val messageId: String,
        val newText: String,
        val editedAt: Long
    ) : RealtimeEvent()

    data class MessageDeleted(
        val conversationId: String,
        val messageId: String,
        val mode: String
    ) : RealtimeEvent()

    data class MessageRead(
        val conversationId: String,
        val lastReadMessageId: String,
        val readAt: Long
    ) : RealtimeEvent()

    // Typing Indicators
    data class UserTyping(
        val conversationId: String,
        val userId: String,
        val username: String,
        val isTyping: Boolean
    ) : RealtimeEvent()

    // Presence
    data class UserPresence(
        val userId: String,
        val isOnline: Boolean,
        val lastSeen: Long
    ) : RealtimeEvent()

    // Reactions
    data class ReactionAdded(
        val messageId: String,
        val userId: String,
        val reaction: String
    ) : RealtimeEvent()

    data class ReactionRemoved(
        val messageId: String,
        val userId: String,
        val reaction: String
    ) : RealtimeEvent()

    // Channel Posts
    data class ChannelPostPublished(
        val channelId: String,
        val postId: String,
        val authorName: String,
        val text: String,
        val promptText: String,
        val mediaUrl: String,
        val createdAt: Long
    ) : RealtimeEvent()
}

/**
 * Dedicated RealtimeManager abstraction for Tarhi Noo Messenger.
 * Provider-independent interface supporting WebSockets, Firebase Realtime,
 * or Server-Sent Events with exponential backoff reconnect logic.
 */
interface RealtimeManager {
    val connectionState: StateFlow<RealtimeConnectionState>
    val events: Flow<RealtimeEvent>

    fun connect(token: String, userId: String)
    fun disconnect()
    fun sendTyping(conversationId: String, isTyping: Boolean)
    fun subscribeToConversation(conversationId: String)
    fun unsubscribeFromConversation(conversationId: String)
    fun subscribeToChannel(channelId: String)
    fun unsubscribeFromChannel(channelId: String)
}
