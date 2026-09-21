package com.example.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "displayName") val displayName: String,
    @Json(name = "avatarUrl") val avatarUrl: String = "",
    @Json(name = "bio") val bio: String = "",
    @Json(name = "isOnline") val isOnline: Boolean = false,
    @Json(name = "lastSeen") val lastSeen: Long = System.currentTimeMillis(),
    @Json(name = "isVerified") val isVerified: Boolean = false,
    @Json(name = "createdAt") val createdAt: Long = System.currentTimeMillis(),
    @Json(name = "updatedAt") val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class ConversationDto(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String, // "PRIVATE", "GROUP", "CHANNEL", "AI"
    @Json(name = "title") val title: String,
    @Json(name = "avatarUrl") val avatarUrl: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "directUserId") val directUserId: String? = null,
    @Json(name = "unreadCount") val unreadCount: Int = 0,
    @Json(name = "lastMessageText") val lastMessageText: String = "",
    @Json(name = "lastMessageSenderName") val lastMessageSenderName: String = "",
    @Json(name = "lastMessageTimestamp") val lastMessageTimestamp: Long = System.currentTimeMillis(),
    @Json(name = "createdAt") val createdAt: Long = System.currentTimeMillis(),
    @Json(name = "updatedAt") val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    @Json(name = "id") val id: String,
    @Json(name = "clientRequestId") val clientRequestId: String? = null,
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "senderId") val senderId: String,
    @Json(name = "senderDisplayName") val senderDisplayName: String = "",
    @Json(name = "text") val text: String,
    @Json(name = "messageType") val messageType: String = "TEXT",
    @Json(name = "deliveryStatus") val deliveryStatus: String = "SENT", // "PENDING", "SENT", "DELIVERED", "READ", "FAILED"
    @Json(name = "replyToMessageId") val replyToMessageId: String? = null,
    @Json(name = "replyToText") val replyToText: String? = null,
    @Json(name = "replyToSenderName") val replyToSenderName: String? = null,
    @Json(name = "forwardedFromMessageId") val forwardedFromMessageId: String? = null,
    @Json(name = "forwardedFromSenderName") val forwardedFromSenderName: String? = null,
    @Json(name = "isPinned") val isPinned: Boolean = false,
    @Json(name = "isAiGenerated") val isAiGenerated: Boolean = false,
    @Json(name = "aiActionPrompt") val aiActionPrompt: String? = null,
    @Json(name = "aiTargetModule") val aiTargetModule: String? = null,
    @Json(name = "readAt") val readAt: Long? = null,
    @Json(name = "editedAt") val editedAt: Long? = null,
    @Json(name = "deletedAt") val deletedAt: Long? = null,
    @Json(name = "createdAt") val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class ReactionDto(
    @Json(name = "id") val id: String,
    @Json(name = "messageId") val messageId: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "reaction") val reaction: String,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class GroupDto(
    @Json(name = "id") val id: String,
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "avatarUrl") val avatarUrl: String = "",
    @Json(name = "ownerId") val ownerId: String,
    @Json(name = "memberCount") val memberCount: Int = 1,
    @Json(name = "inviteCode") val inviteCode: String = "",
    @Json(name = "createdAt") val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class ChannelDto(
    @Json(name = "id") val id: String,
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "name") val name: String,
    @Json(name = "username") val username: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "avatarUrl") val avatarUrl: String = "",
    @Json(name = "coverUrl") val coverUrl: String = "",
    @Json(name = "ownerId") val ownerId: String,
    @Json(name = "subscriberCount") val subscriberCount: Int = 1,
    @Json(name = "isPublic") val isPublic: Boolean = true,
    @Json(name = "createdAt") val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class ChannelPostDto(
    @Json(name = "id") val id: String,
    @Json(name = "channelId") val channelId: String,
    @Json(name = "authorId") val authorId: String,
    @Json(name = "authorName") val authorName: String,
    @Json(name = "text") val text: String,
    @Json(name = "mediaUrl") val mediaUrl: String = "",
    @Json(name = "mediaType") val mediaType: String = "TEXT",
    @Json(name = "promptText") val promptText: String = "",
    @Json(name = "viewCount") val viewCount: Int = 1,
    @Json(name = "reactionCount") val reactionCount: Int = 0,
    @Json(name = "isPinned") val isPinned: Boolean = false,
    @Json(name = "createdAt") val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class AttachmentUploadRequest(
    @Json(name = "fileName") val fileName: String,
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "sizeBytes") val sizeBytes: Long,
    @Json(name = "conversationId") val conversationId: String
)

@JsonClass(generateAdapter = true)
data class AttachmentUploadResponse(
    @Json(name = "uploadUrl") val uploadUrl: String,
    @Json(name = "fileId") val fileId: String,
    @Json(name = "publicUrl") val publicUrl: String,
    @Json(name = "expiresAt") val expiresAt: Long
)

@JsonClass(generateAdapter = true)
data class ReportDto(
    @Json(name = "id") val id: String,
    @Json(name = "reporterId") val reporterId: String,
    @Json(name = "targetType") val targetType: String,
    @Json(name = "targetId") val targetId: String,
    @Json(name = "reasonCategory") val reasonCategory: String,
    @Json(name = "details") val details: String = "",
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @Json(name = "status") val status: String = "OPEN"
)
