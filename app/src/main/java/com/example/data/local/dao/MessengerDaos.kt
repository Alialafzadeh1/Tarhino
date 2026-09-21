package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BlockedUserEntity
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ChannelPostEntity
import com.example.data.local.entity.ConversationMemberEntity
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.GroupPermissionEntity
import com.example.data.local.entity.MessageAttachmentEntity
import com.example.data.local.entity.MessageReactionEntity
import com.example.data.local.entity.MessengerConversationEntity
import com.example.data.local.entity.MessengerMessageEntity
import com.example.data.local.entity.ReportEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessengerUserDao {
    @Query("SELECT * FROM users WHERE isBlocked = 0 ORDER BY displayName ASC")
    fun getAllActiveUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isContact = 1 AND isBlocked = 0 ORDER BY displayName ASC")
    fun getContacts(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isBlocked = 1 ORDER BY displayName ASC")
    fun getBlockedUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%' ORDER BY displayName ASC")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isBlocked = :blocked WHERE id = :userId")
    suspend fun setUserBlocked(userId: Long, blocked: Boolean)

    @Query("UPDATE users SET isContact = :isContact WHERE id = :userId")
    suspend fun setUserContact(userId: Long, isContact: Boolean)

    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun setUserOnlineStatus(userId: Long, isOnline: Boolean, lastSeen: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface MessengerConversationDao {
    @Query("SELECT * FROM messenger_conversations WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllConversations(): Flow<List<MessengerConversationEntity>>

    @Query("SELECT * FROM messenger_conversations WHERE type = :type AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getConversationsByType(type: String): Flow<List<MessengerConversationEntity>>

    @Query("SELECT * FROM messenger_conversations WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedConversations(): Flow<List<MessengerConversationEntity>>

    @Query("SELECT * FROM messenger_conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): MessengerConversationEntity?

    @Query("SELECT * FROM messenger_conversations WHERE directUserId = :userId AND type = 'PRIVATE' LIMIT 1")
    suspend fun getPrivateConversationWithUser(userId: Long): MessengerConversationEntity?

    @Query("SELECT SUM(unreadCount) FROM messenger_conversations WHERE isMuted = 0")
    fun getTotalUnreadCount(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: MessengerConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: MessengerConversationEntity)

    @Query("UPDATE messenger_conversations SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("UPDATE messenger_conversations SET isMuted = :muted WHERE id = :id")
    suspend fun setMuted(id: Long, muted: Boolean)

    @Query("UPDATE messenger_conversations SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Query("UPDATE messenger_conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE messenger_conversations SET lastMessageText = :lastText, lastMessageSenderName = :sender, lastMessageTimestamp = :time, updatedAt = :time, unreadCount = unreadCount + :unreadDelta WHERE id = :id")
    suspend fun updateLastMessage(id: Long, lastText: String, sender: String, time: Long, unreadDelta: Int)

    @Query("DELETE FROM messenger_conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)

    @Query("SELECT COUNT(*) FROM messenger_conversations")
    suspend fun getConversationCount(): Int
}

@Dao
interface MessengerMessageDao {
    @Query("SELECT * FROM messenger_messages WHERE conversationId = :convId AND deletedAt IS NULL ORDER BY createdAt ASC")
    fun getMessagesForConversation(convId: Long): Flow<List<MessengerMessageEntity>>

    @Query("SELECT * FROM messenger_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessengerMessageEntity?

    @Query("SELECT * FROM messenger_messages WHERE conversationId = :convId AND isPinned = 1 AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getPinnedMessages(convId: Long): Flow<List<MessengerMessageEntity>>

    @Query("SELECT * FROM messenger_messages WHERE conversationId = :convId AND text LIKE '%' || :query || '%' AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun searchMessagesInConversation(convId: Long, query: String): Flow<List<MessengerMessageEntity>>

    @Query("SELECT * FROM messenger_messages WHERE text LIKE '%' || :query || '%' AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun globalSearchMessages(query: String): Flow<List<MessengerMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessengerMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessengerMessageEntity>)

    @Update
    suspend fun updateMessage(message: MessengerMessageEntity)

    @Query("UPDATE messenger_messages SET isPinned = :pinned WHERE id = :id")
    suspend fun setMessagePinned(id: Long, pinned: Boolean)

    @Query("UPDATE messenger_messages SET text = :newText, editedAt = :editedTime WHERE id = :id")
    suspend fun editMessageText(id: Long, newText: String, editedTime: Long = System.currentTimeMillis())

    @Query("UPDATE messenger_messages SET deletedAt = :deletedTime, text = 'این پیام حذف شد' WHERE id = :id")
    suspend fun softDeleteMessage(id: Long, deletedTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM messenger_messages WHERE id = :id")
    suspend fun deleteMessagePermanently(id: Long)

    @Query("DELETE FROM messenger_messages WHERE conversationId = :convId")
    suspend fun deleteAllMessagesInConversation(convId: Long)
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE conversationId = :convId LIMIT 1")
    suspend fun getGroupByConversationId(convId: Long): GroupEntity?

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: Long): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteGroup(id: Long)

    // Members
    @Query("SELECT * FROM conversation_members WHERE conversationId = :convId")
    fun getMembersForConversation(convId: Long): Flow<List<ConversationMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: ConversationMemberEntity): Long

    @Query("DELETE FROM conversation_members WHERE conversationId = :convId AND userId = :userId")
    suspend fun removeMember(convId: Long, userId: Long)

    @Query("UPDATE conversation_members SET role = :newRole WHERE conversationId = :convId AND userId = :userId")
    suspend fun updateMemberRole(convId: Long, userId: Long, newRole: String)

    // Permissions
    @Query("SELECT * FROM group_permissions WHERE groupId = :groupId LIMIT 1")
    suspend fun getGroupPermissions(groupId: Long): GroupPermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermissions(perms: GroupPermissionEntity)

    @Update
    suspend fun updatePermissions(perms: GroupPermissionEntity)
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isSubscribed = 1 ORDER BY name ASC")
    fun getSubscribedChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE conversationId = :convId LIMIT 1")
    suspend fun getChannelByConversationId(convId: Long): ChannelEntity?

    @Query("SELECT * FROM channels WHERE id = :channelId LIMIT 1")
    suspend fun getChannelById(channelId: Long): ChannelEntity?

    @Query("SELECT * FROM channels WHERE username = :username LIMIT 1")
    suspend fun getChannelByUsername(username: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Query("UPDATE channels SET isSubscribed = :subscribed, subscriberCount = subscriberCount + (CASE WHEN :subscribed = 1 THEN 1 ELSE -1 END) WHERE id = :channelId")
    suspend fun setSubscribed(channelId: Long, subscribed: Boolean)

    @Query("DELETE FROM channels WHERE id = :id")
    suspend fun deleteChannel(id: Long)

    // Channel Posts
    @Query("SELECT * FROM channel_posts WHERE channelId = :channelId ORDER BY createdAt DESC")
    fun getPostsForChannel(channelId: Long): Flow<List<ChannelPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ChannelPostEntity): Long

    @Update
    suspend fun updatePost(post: ChannelPostEntity)

    @Query("DELETE FROM channel_posts WHERE id = :postId")
    suspend fun deletePost(postId: Long)

    @Query("UPDATE channel_posts SET reactionCount = reactionCount + 1 WHERE id = :postId")
    suspend fun incrementReaction(postId: Long)
}

@Dao
interface ReactionDao {
    @Query("SELECT * FROM message_reactions WHERE messageId = :messageId")
    fun getReactionsForMessage(messageId: Long): Flow<List<MessageReactionEntity>>

    @Query("SELECT * FROM message_reactions WHERE messageId = :messageId AND userId = :userId AND reaction = :reaction LIMIT 1")
    suspend fun getReaction(messageId: Long, userId: Long, reaction: String): MessageReactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: MessageReactionEntity): Long

    @Query("DELETE FROM message_reactions WHERE messageId = :messageId AND userId = :userId AND reaction = :reaction")
    suspend fun deleteReaction(messageId: Long, userId: Long, reaction: String)
}

@Dao
interface ModerationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedUser(blocked: BlockedUserEntity): Long

    @Query("DELETE FROM blocked_users WHERE userId = :userId")
    suspend fun removeBlockedUser(userId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_users WHERE userId = :userId)")
    suspend fun isUserBlocked(userId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>
}
