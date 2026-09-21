package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "IMAGE", "VIDEO", "TEXT", "CHAT"
    val title: String,
    val description: String,
    val prompt: String,
    val negativePrompt: String = "",
    val language: String = "fa",
    val categoryId: String = "all",
    val subcategoryId: String = "",
    val tags: String = "",
    val style: String = "",
    val lighting: String = "",
    val camera: String = "",
    val lens: String = "",
    val composition: String = "",
    val environment: String = "",
    val material: String = "",
    val color: String = "",
    val mood: String = "",
    val quality: String = "8K, cinematic",
    val thumbnail: String = "",
    val preview: String = "",
    val author: String = "Tarhineh Media",
    val isPremium: Boolean = false,
    val isFeatured: Boolean = false,
    val viewCount: Int = 120,
    val copyCount: Int = 14,
    val favoriteCount: Int = 28,
    val isSaved: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
