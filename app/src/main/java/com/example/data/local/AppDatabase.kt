package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AssetDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.CommunityDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.PromptDao
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.CommunityPostEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.PromptEntity

@Database(
    entities = [
        PromptEntity::class,
        ProjectEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        AssetEntity::class,
        HistoryEntity::class,
        NotificationEntity::class,
        CommunityPostEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao
    abstract fun projectDao(): ProjectDao
    abstract fun chatDao(): ChatDao
    abstract fun assetDao(): AssetDao
    abstract fun historyDao(): HistoryDao
    abstract fun notificationDao(): NotificationDao
    abstract fun communityDao(): CommunityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tarhi_noo_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
