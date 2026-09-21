package com.example.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "user") val user: UserDto,
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "expiresIn") val expiresIn: Long
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "username") val username: String,
    @Json(name = "displayName") val displayName: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refreshToken") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "requestId") val requestId: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "code") val code: String,
    @Json(name = "message") val message: String,
    @Json(name = "details") val details: String? = null
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "clientRequestId") val clientRequestId: String,
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "text") val text: String,
    @Json(name = "messageType") val messageType: String = "TEXT",
    @Json(name = "replyToMessageId") val replyToMessageId: String? = null,
    @Json(name = "replyToText") val replyToText: String? = null,
    @Json(name = "replyToSenderName") val replyToSenderName: String? = null,
    @Json(name = "aiActionPrompt") val aiActionPrompt: String? = null,
    @Json(name = "aiTargetModule") val aiTargetModule: String? = null
)

@JsonClass(generateAdapter = true)
data class EditMessageRequest(
    @Json(name = "newText") val newText: String
)

@JsonClass(generateAdapter = true)
data class MarkReadRequest(
    @Json(name = "lastReadMessageId") val lastReadMessageId: String,
    @Json(name = "readAt") val readAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class CreateGroupRequest(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "memberUserIds") val memberUserIds: List<String>
)

@JsonClass(generateAdapter = true)
data class CreateChannelRequest(
    @Json(name = "name") val name: String,
    @Json(name = "username") val username: String,
    @Json(name = "description") val description: String = ""
)

@JsonClass(generateAdapter = true)
data class PublishChannelPostRequest(
    @Json(name = "text") val text: String,
    @Json(name = "promptText") val promptText: String = "",
    @Json(name = "mediaUrl") val mediaUrl: String = "",
    @Json(name = "mediaType") val mediaType: String = "TEXT"
)

@JsonClass(generateAdapter = true)
data class RealtimeEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "conversationId") val conversationId: String? = null,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "message") val message: MessageDto? = null,
    @Json(name = "reaction") val reaction: ReactionDto? = null,
    @Json(name = "channelPost") val channelPost: ChannelPostDto? = null,
    @Json(name = "isTyping") val isTyping: Boolean? = null,
    @Json(name = "isOnline") val isOnline: Boolean? = null,
    @Json(name = "lastSeen") val lastSeen: Long? = null,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)
