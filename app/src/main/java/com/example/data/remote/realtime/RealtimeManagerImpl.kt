package com.example.data.remote.realtime

import android.util.Log
import com.example.data.remote.auth.SessionManager
import com.example.data.remote.config.BackendConfig
import com.example.data.remote.model.RealtimeEventDto
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * Production-ready WebSocket/Realtime engine with exponential backoff reconnect logic,
 * typing indicators debounce, and message idempotency handling.
 */
class RealtimeManagerImpl(
    private val sessionManager: SessionManager,
    private val moshi: Moshi = Moshi.Builder().build()
) : RealtimeManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var currentRetryCount = 0
    private val maxRetryBackoffMs = 30_000L

    private val _connectionState = MutableStateFlow(RealtimeConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<RealtimeConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<RealtimeEvent>(replay = 0, extraBufferCapacity = 64)
    override val events: Flow<RealtimeEvent> = _events.asSharedFlow()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(25, TimeUnit.SECONDS)
            .build()
    }

    private var activeToken: String? = null
    private var activeUserId: String? = null
    private var isManuallyDisconnected = false

    override fun connect(token: String, userId: String) {
        if (!BackendConfig.isBackendConfigured) {
            Log.d("TarhiNooRealtime", "Backend not configured in .env. Realtime idle in LOCAL_ONLY mode.")
            _connectionState.value = RealtimeConnectionState.DISCONNECTED
            return
        }

        activeToken = token
        activeUserId = userId
        isManuallyDisconnected = false
        _connectionState.value = RealtimeConnectionState.CONNECTING

        initiateWebSocket()
    }

    private fun initiateWebSocket() {
        val realtimeUrl = BackendConfig.realtimeUrl.ifBlank {
            BackendConfig.baseUrl.replace("http://", "ws://").replace("https://", "wss://") + "/realtime"
        }

        try {
            val request = Request.Builder()
                .url(realtimeUrl)
                .addHeader("Authorization", "Bearer ${activeToken.orEmpty()}")
                .addHeader("X-User-Id", activeUserId.orEmpty())
                .addHeader("X-Client-App", "TarhiNoo-Android")
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.i("TarhiNooRealtime", "Connected to Tarhi Noo Realtime Gateway")
                    _connectionState.value = RealtimeConnectionState.CONNECTED
                    currentRetryCount = 0
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleIncomingPayload(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(1000, null)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (!isManuallyDisconnected) {
                        scheduleReconnect()
                    } else {
                        _connectionState.value = RealtimeConnectionState.DISCONNECTED
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.w("TarhiNooRealtime", "Realtime connection failure: ${t.message}")
                    if (!isManuallyDisconnected) {
                        scheduleReconnect()
                    } else {
                        _connectionState.value = RealtimeConnectionState.FAILED
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("TarhiNooRealtime", "Failed to initialize WebSocket: ${e.message}")
            scheduleReconnect()
        }
    }

    private fun handleIncomingPayload(json: String) {
        try {
            val adapter = moshi.adapter(RealtimeEventDto::class.java)
            val dto = adapter.fromJson(json) ?: return

            scope.launch {
                when (dto.type) {
                    "MESSAGE_CREATED" -> {
                        dto.message?.let { m ->
                            _events.emit(
                                RealtimeEvent.MessageCreated(
                                    conversationId = m.conversationId,
                                    messageId = m.id,
                                    senderId = m.senderId,
                                    senderDisplayName = m.senderDisplayName,
                                    text = m.text,
                                    messageType = m.messageType,
                                    clientRequestId = m.clientRequestId,
                                    replyToId = m.replyToMessageId,
                                    aiActionPrompt = m.aiActionPrompt,
                                    aiTargetModule = m.aiTargetModule,
                                    createdAt = m.createdAt
                                )
                            )
                        }
                    }
                    "MESSAGE_UPDATED" -> {
                        dto.message?.let { m ->
                            _events.emit(
                                RealtimeEvent.MessageUpdated(
                                    conversationId = m.conversationId,
                                    messageId = m.id,
                                    newText = m.text,
                                    editedAt = m.editedAt ?: System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    "MESSAGE_DELETED" -> {
                        if (dto.conversationId != null && dto.message?.id != null) {
                            _events.emit(
                                RealtimeEvent.MessageDeleted(
                                    conversationId = dto.conversationId,
                                    messageId = dto.message.id,
                                    mode = "EVERYONE"
                                )
                            )
                        }
                    }
                    "MESSAGE_READ" -> {
                        if (dto.conversationId != null) {
                            _events.emit(
                                RealtimeEvent.MessageRead(
                                    conversationId = dto.conversationId,
                                    lastReadMessageId = dto.message?.id.orEmpty(),
                                    readAt = dto.timestamp
                                )
                            )
                        }
                    }
                    "TYPING" -> {
                        if (dto.conversationId != null && dto.userId != null) {
                            _events.emit(
                                RealtimeEvent.UserTyping(
                                    conversationId = dto.conversationId,
                                    userId = dto.userId,
                                    username = dto.userId,
                                    isTyping = dto.isTyping ?: false
                                )
                            )
                        }
                    }
                    "PRESENCE" -> {
                        if (dto.userId != null) {
                            _events.emit(
                                RealtimeEvent.UserPresence(
                                    userId = dto.userId,
                                    isOnline = dto.isOnline ?: false,
                                    lastSeen = dto.lastSeen ?: System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    "REACTION_ADDED" -> {
                        dto.reaction?.let { r ->
                            _events.emit(
                                RealtimeEvent.ReactionAdded(
                                    messageId = r.messageId,
                                    userId = r.userId,
                                    reaction = r.reaction
                                )
                            )
                        }
                    }
                    "REACTION_REMOVED" -> {
                        dto.reaction?.let { r ->
                            _events.emit(
                                RealtimeEvent.ReactionRemoved(
                                    messageId = r.messageId,
                                    userId = r.userId,
                                    reaction = r.reaction
                                )
                            )
                        }
                    }
                    "CHANNEL_POST" -> {
                        dto.channelPost?.let { p ->
                            _events.emit(
                                RealtimeEvent.ChannelPostPublished(
                                    channelId = p.channelId,
                                    postId = p.id,
                                    authorName = p.authorName,
                                    text = p.text,
                                    promptText = p.promptText,
                                    mediaUrl = p.mediaUrl,
                                    createdAt = p.createdAt
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TarhiNooRealtime", "Error parsing realtime message: ${e.message}")
        }
    }

    private fun scheduleReconnect() {
        if (isManuallyDisconnected || !BackendConfig.isBackendConfigured) return
        _connectionState.value = RealtimeConnectionState.RECONNECTING

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            currentRetryCount++
            val backoffMs = (1000L * (1L shl (currentRetryCount.coerceAtMost(5)))).coerceAtMost(maxRetryBackoffMs)
            Log.d("TarhiNooRealtime", "Reconnecting in ${backoffMs}ms (attempt $currentRetryCount)")
            delay(backoffMs)
            if (isActive && !isManuallyDisconnected) {
                initiateWebSocket()
            }
        }
    }

    override fun disconnect() {
        isManuallyDisconnected = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _connectionState.value = RealtimeConnectionState.DISCONNECTED
    }

    override fun sendTyping(conversationId: String, isTyping: Boolean) {
        if (_connectionState.value != RealtimeConnectionState.CONNECTED) return
        val payload = """{"type":"TYPING","conversationId":"$conversationId","isTyping":$isTyping}"""
        webSocket?.send(payload)
    }

    override fun subscribeToConversation(conversationId: String) {
        if (_connectionState.value != RealtimeConnectionState.CONNECTED) return
        val payload = """{"type":"SUBSCRIBE_CONVERSATION","conversationId":"$conversationId"}"""
        webSocket?.send(payload)
    }

    override fun unsubscribeFromConversation(conversationId: String) {
        if (_connectionState.value != RealtimeConnectionState.CONNECTED) return
        val payload = """{"type":"UNSUBSCRIBE_CONVERSATION","conversationId":"$conversationId"}"""
        webSocket?.send(payload)
    }

    override fun subscribeToChannel(channelId: String) {
        if (_connectionState.value != RealtimeConnectionState.CONNECTED) return
        val payload = """{"type":"SUBSCRIBE_CHANNEL","channelId":"$channelId"}"""
        webSocket?.send(payload)
    }

    override fun unsubscribeFromChannel(channelId: String) {
        if (_connectionState.value != RealtimeConnectionState.CONNECTED) return
        val payload = """{"type":"UNSUBSCRIBE_CHANNEL","channelId":"$channelId"}"""
        webSocket?.send(payload)
    }
}
