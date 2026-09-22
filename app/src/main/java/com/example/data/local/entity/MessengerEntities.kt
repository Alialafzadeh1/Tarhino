package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String, // stored normalized without @ e.g. "ali", displayed as "@ali"
    val displayName: String,
    val avatarUrl: String = "",
    val bio: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val isBlocked: Boolean = false,
    val isContact: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messenger_conversations",
    indices = [
        Index(value = ["type"]),
        Index(value = ["updatedAt"])
    ]
)
data class MessengerConversationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String = "PRIVATE", // "PRIVATE", "GROUP", "CHANNEL", "AI"
    val title: String,
    val avatarUrl: String = "",
    val description: String = "",
    val directUserId: String? = null, // for PRIVATE chat: references the other UserEntity.id (String UUID)
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessageText: String = "",
    val lastMessageSenderName: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messenger_messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["createdAt"]),
        Index(value = ["clientRequestId"]),
        Index(value = ["serverId"])
    ]
)
data class MessengerMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val senderId: String, // current user ID UUID, "tarhinoo_ai", or remote sender UUID
    val senderDisplayName: String = "",
    val text: String,
    val messageType: String = "TEXT", // "TEXT", "IMAGE", "VIDEO", "FILE", "AUDIO", "VOICE", "SYSTEM", "AI_RESULT"
    val deliveryStatus: String = "SENT", // "LOCAL_ONLY", "PENDING", "SENT", "DELIVERED", "READ", "FAILED"
    val serverId: String? = null,
    val clientRequestId: String? = null,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSenderName: String? = null,
    val forwardedFromMessageId: String? = null,
    val forwardedFromSenderName: String? = null,
    val isPinned: Boolean = false,
    val isAiGenerated: Boolean = false,
    val aiActionPrompt: String? = null,
    val aiTargetModule: String? = null, // "PROMPT_BUILDER", "NAVA_STUDIO", null
    val readAt: Long? = null,
    val editedAt: Long? = null,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversation_members",
    indices = [
        Index(value = ["conversationId", "userId"], unique = true)
    ]
)
data class ConversationMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val userId: String,
    val role: String = "MEMBER", // "OWNER", "ADMIN", "MODERATOR", "MEMBER"
    val joinedAt: Long = System.currentTimeMillis(),
    val lastReadMessageId: String = "",
    val notificationsEnabled: Boolean = true
)

@Entity(
    tableName = "groups",
    indices = [Index(value = ["conversationId"])]
)
data class GroupEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val name: String,
    val description: String = "",
    val avatarUrl: String = "",
    val ownerId: String = "",
    val isPrivate: Boolean = false,
    val inviteCode: String = "",
    val memberCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "group_permissions",
    indices = [Index(value = ["groupId"], unique = true)]
)
data class GroupPermissionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val sendMessages: Boolean = true,
    val sendMedia: Boolean = true,
    val addMembers: Boolean = true,
    val removeMembers: Boolean = false,
    val pinMessages: Boolean = false,
    val editGroup: Boolean = false,
    val deleteMessages: Boolean = false,
    val manageAdmins: Boolean = false,
    val manageMembers: Boolean = false
)

@Entity(
    tableName = "channels",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["username"], unique = true)
    ]
)
data class ChannelEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val name: String,
    val username: String, // e.g. "tarhineh_art", displayed as @tarhineh_art
    val description: String = "",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val ownerId: String = "",
    val subscriberCount: Int = 1,
    val isPublic: Boolean = true,
    val inviteLink: String = "",
    val isSubscribed: Boolean = false,
    val isMuted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "channel_posts",
    indices = [Index(value = ["channelId"])]
)
data class ChannelPostEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val channelId: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val mediaUrl: String = "",
    val mediaType: String = "TEXT", // "TEXT", "IMAGE", "VIDEO", "PROMPT"
    val promptText: String = "",
    val viewCount: Int = 1,
    val reactionCount: Int = 0,
    val isPinned: Boolean = false,
    val editedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "message_reactions",
    indices = [
        Index(value = ["messageId", "userId", "reaction"], unique = true)
    ]
)
data class MessageReactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: String,
    val userId: String,
    val reaction: String, // ❤️, 👍, 🔥, 😂, 😍, 😮, 😢, 👎
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "message_attachments",
    indices = [Index(value = ["messageId"])]
)
data class MessageAttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: String,
    val type: String, // "IMAGE", "VIDEO", "AUDIO", "DOCUMENT", "AI_ASSET"
    val localUri: String = "",
    val remoteUrl: String = "",
    val fileName: String = "",
    val mimeType: String = "",
    val sizeBytes: Long = 0L,
    val thumbnailUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "blocked_users",
    indices = [Index(value = ["userId"], unique = true)]
)
data class BlockedUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val blockedAt: Long = System.currentTimeMillis(),
    val reason: String = ""
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetType: String, // "USER", "MESSAGE", "GROUP", "CHANNEL"
    val targetId: String,
    val reasonCategory: String, // "SPAM", "HARASSMENT", "INAPPROPRIATE_CONTENT", "IMPERSONATION", "OTHER"
    val details: String = "",
    val reportedByUserId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // "PENDING", "REVIEWED", "DISMISSED", "RESOLVED"
)
