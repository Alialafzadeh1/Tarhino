package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AssetDao
import com.example.data.local.dao.ChannelDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.CommunityDao
import com.example.data.local.dao.GroupDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.MessengerConversationDao
import com.example.data.local.dao.MessengerMessageDao
import com.example.data.local.dao.MessengerUserDao
import com.example.data.local.dao.ModerationDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.PromptDao
import com.example.data.local.dao.ReactionDao
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.BlockedUserEntity
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ChannelPostEntity
import com.example.data.local.entity.CommunityPostEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.ConversationMemberEntity
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.GroupPermissionEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.MessageAttachmentEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.MessageReactionEntity
import com.example.data.local.entity.MessengerConversationEntity
import com.example.data.local.entity.MessengerMessageEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.PromptEntity
import com.example.data.local.entity.ReportEntity
import com.example.data.local.entity.UserEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PromptEntity::class,
        ProjectEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        AssetEntity::class,
        HistoryEntity::class,
        NotificationEntity::class,
        CommunityPostEntity::class,
        UserEntity::class,
        MessengerConversationEntity::class,
        MessengerMessageEntity::class,
        ConversationMemberEntity::class,
        GroupEntity::class,
        GroupPermissionEntity::class,
        ChannelEntity::class,
        ChannelPostEntity::class,
        MessageReactionEntity::class,
        MessageAttachmentEntity::class,
        BlockedUserEntity::class,
        ReportEntity::class
    ],
    version = 2,
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

    // Messenger DAOs
    abstract fun messengerUserDao(): MessengerUserDao
    abstract fun messengerConversationDao(): MessengerConversationDao
    abstract fun messengerMessageDao(): MessengerMessageDao
    abstract fun groupDao(): GroupDao
    abstract fun channelDao(): ChannelDao
    abstract fun reactionDao(): ReactionDao
    abstract fun moderationDao(): ModerationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        username TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        avatarUrl TEXT NOT NULL,
                        bio TEXT NOT NULL,
                        isOnline INTEGER NOT NULL,
                        lastSeen INTEGER NOT NULL,
                        isVerified INTEGER NOT NULL,
                        isBlocked INTEGER NOT NULL,
                        isContact INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_username ON users(username)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS messenger_conversations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        avatarUrl TEXT NOT NULL,
                        description TEXT NOT NULL,
                        directUserId INTEGER,
                        isPinned INTEGER NOT NULL,
                        isMuted INTEGER NOT NULL,
                        isArchived INTEGER NOT NULL,
                        unreadCount INTEGER NOT NULL,
                        lastMessageText TEXT NOT NULL,
                        lastMessageSenderName TEXT NOT NULL,
                        lastMessageTimestamp INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_messenger_conversations_type ON messenger_conversations(type)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_messenger_conversations_updatedAt ON messenger_conversations(updatedAt)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS messenger_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        conversationId INTEGER NOT NULL,
                        senderId INTEGER NOT NULL,
                        senderDisplayName TEXT NOT NULL,
                        text TEXT NOT NULL,
                        messageType TEXT NOT NULL,
                        deliveryStatus TEXT NOT NULL,
                        replyToMessageId INTEGER,
                        replyToText TEXT,
                        replyToSenderName TEXT,
                        forwardedFromMessageId INTEGER,
                        forwardedFromSenderName TEXT,
                        isPinned INTEGER NOT NULL,
                        isAiGenerated INTEGER NOT NULL,
                        aiActionPrompt TEXT,
                        aiTargetModule TEXT,
                        readAt INTEGER,
                        editedAt INTEGER,
                        deletedAt INTEGER,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_messenger_messages_conversationId ON messenger_messages(conversationId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_messenger_messages_createdAt ON messenger_messages(createdAt)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS conversation_members (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        conversationId INTEGER NOT NULL,
                        userId INTEGER NOT NULL,
                        role TEXT NOT NULL,
                        joinedAt INTEGER NOT NULL,
                        lastReadMessageId INTEGER NOT NULL,
                        notificationsEnabled INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_conversation_members_conversationId_userId ON conversation_members(conversationId, userId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS groups (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        conversationId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        avatarUrl TEXT NOT NULL,
                        ownerId INTEGER NOT NULL,
                        isPrivate INTEGER NOT NULL,
                        inviteCode TEXT NOT NULL,
                        memberCount INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_groups_conversationId ON groups(conversationId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS group_permissions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        groupId INTEGER NOT NULL,
                        sendMessages INTEGER NOT NULL,
                        sendMedia INTEGER NOT NULL,
                        addMembers INTEGER NOT NULL,
                        removeMembers INTEGER NOT NULL,
                        pinMessages INTEGER NOT NULL,
                        editGroup INTEGER NOT NULL,
                        deleteMessages INTEGER NOT NULL,
                        manageAdmins INTEGER NOT NULL,
                        manageMembers INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_group_permissions_groupId ON group_permissions(groupId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS channels (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        conversationId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        username TEXT NOT NULL,
                        description TEXT NOT NULL,
                        avatarUrl TEXT NOT NULL,
                        coverUrl TEXT NOT NULL,
                        ownerId INTEGER NOT NULL,
                        subscriberCount INTEGER NOT NULL,
                        isPublic INTEGER NOT NULL,
                        inviteLink TEXT NOT NULL,
                        isSubscribed INTEGER NOT NULL,
                        isMuted INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_channels_conversationId ON channels(conversationId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_channels_username ON channels(username)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS channel_posts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        channelId INTEGER NOT NULL,
                        authorId INTEGER NOT NULL,
                        authorName TEXT NOT NULL,
                        text TEXT NOT NULL,
                        mediaUrl TEXT NOT NULL,
                        mediaType TEXT NOT NULL,
                        promptText TEXT NOT NULL,
                        viewCount INTEGER NOT NULL,
                        reactionCount INTEGER NOT NULL,
                        isPinned INTEGER NOT NULL,
                        editedAt INTEGER,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_channel_posts_channelId ON channel_posts(channelId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS message_reactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        messageId INTEGER NOT NULL,
                        userId INTEGER NOT NULL,
                        reaction TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_message_reactions_messageId_userId_reaction ON message_reactions(messageId, userId, reaction)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS message_attachments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        messageId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        localUri TEXT NOT NULL,
                        remoteUrl TEXT NOT NULL,
                        fileName TEXT NOT NULL,
                        mimeType TEXT NOT NULL,
                        sizeBytes INTEGER NOT NULL,
                        thumbnailUrl TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_message_attachments_messageId ON message_attachments(messageId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS blocked_users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        blockedAt INTEGER NOT NULL,
                        reason TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_blocked_users_userId ON blocked_users(userId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reports (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        targetType TEXT NOT NULL,
                        targetId INTEGER NOT NULL,
                        reasonCategory TEXT NOT NULL,
                        details TEXT NOT NULL,
                        reportedByUserId INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        status TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tarhi_noo_database.db"
                ).addMigrations(MIGRATION_1_2)
                 .fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
