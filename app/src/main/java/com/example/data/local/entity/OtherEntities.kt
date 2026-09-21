package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "IMAGE", "VIDEO", "PROMPT", "TEMPLATE", "PRESET", "PROJECT", "REFERENCE"
    val category: String = "General",
    val pathOrUrl: String,
    val tags: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history_entries")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "CHAT", "PROMPT", "IMAGE", "VIDEO", "STUDIO_EDIT", "PROJECT"
    val detail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "AI_COMPLETED", "NEW_MESSAGE", "MENTION", "GROUP_ACTIVITY", "CHANNEL_POST", "PROJECT_UPDATE", "SYSTEM"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorHandle: String,
    val authorAvatar: String = "",
    val content: String,
    val mediaUrl: String = "",
    val promptText: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
