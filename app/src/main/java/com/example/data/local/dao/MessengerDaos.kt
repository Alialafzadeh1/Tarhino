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

@Dao
interface MessengerUserDao {
    @Query("SELECT * FROM users WHERE isBlocked = 0 ORDER BY displayName ASC")
    suspend fun getAllActiveUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE isContact = 1 AND isBlocked = 0 ORDER BY displayName ASC")
    suspend fun getContacts(): List<UserEntity>

    @Query("SELECT * FROM users WHERE isBlocked = 1 ORDER BY displayName ASC")
    suspend fun getBlockedUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%' ORDER BY displayName ASC")
    suspend fun searchUsers(query: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isBlocked = :blocked WHERE id = :userId")
    suspend fun setUserBlocked(userId: String, blocked: Boolean)

    @Query("UPDATE users SET isContact = :isContact WHERE id = :userId")
    suspend fun setUserContact(userId: String, isContact: Boolean)

    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun setUserOnlineStatus(userId: String, isOnline: Boolean, lastSeen: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface MessengerConversationDao {
    @Query("SELECT * FROM messenger_conversations WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getAllConversations(): List<MessengerConversationEntity>

    @Query("SELECT * FROM messenger_conversations WHERE type = :type AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getConversationsByType(type: String): List<MessengerConversationEntity>

    @Query("SELECT * FROM messenger_conversations WHERE isArchived = 1 ORDER BY updatedAt DESC")
    suspend fun getArchivedConversations(): List<MessengerConversationEntity>

    @Query("SELECT * FROM messenger_conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): MessengerConversationEntity?

    @Query("SELECT * FROM messenger_conversations WHERE directUserId = :userId AND type = 'PRIVATE' LIMIT 1")
    suspend fun getPrivateConversationWithUser(userId: String): MessengerConversationEntity?

    @Query("SELECT SUM(unreadCount) FROM messenger_conversations WHERE isMuted = 0")
    suspend fun getTotalUnreadCount(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: MessengerConversationEntity)

    @Update
    suspend fun updateConversation(conversation: MessengerConversationEntity)

    @Query("UPDATE messenger_conversations SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("UPDATE messenger_conversations SET isMuted = :muted WHERE id = :id")
    suspend fun setMuted(id: String, muted: Boolean)

    @Query("UPDATE messenger_conversations SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("UPDATE messenger_conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE messenger_conversations SET lastMessageText = :lastText, lastMessageSenderName = :sender, lastMessageTimestamp = :time, updatedAt = :time, unreadCount = unreadCount + :unreadDelta WHERE id = :id")
    suspend fun updateLastMessage(id: String, lastText: String, sender: String, time: Long, unreadDelta: Int)

    @Query("DELETE FROM messenger_conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)

    @Query("SELECT COUNT(*) FROM messenger_conversations")
    suspend fun getConversationCount(): Int
}

@Dao
interface MessengerMessageDao {
    @Query("SELECT * FROM messenger_messages WHERE conversationId = :convId AND deletedAt IS NULL ORDER BY createdAt ASC")
    suspend fun getMessagesForConversation(convId: String): List<MessengerMessageEntity>

    @Query("SELECT * FROM messenger_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessengerMessageEntity?

    @Query("SELECT * FROM messenger_messages WHERE conversationId = :convId AND isPinned = 1 AND deletedAt IS NULL ORDER BY createdAt DESC")
    suspend fun getPinnedMessages(convId: String): List<MessengerMessageEntity>

    @Query("SELECT * FROM messenger_messages WHERE conversationId = :convId AND text LIKE '%' || :query || '%' AND deletedAt IS NULL ORDER BY createdAt DESC")
    suspend fun searchMessagesInConversation(convId: String, query: String): List<MessengerMessageEntity>

    @Query("SELECT * FROM messenger_messages WHERE text LIKE '%' || :query || '%' AND deletedAt IS NULL ORDER BY createdAt DESC")
    suspend fun globalSearchMessages(query: String): List<MessengerMessageEntity>

    @Query("SELECT * FROM messenger_messages WHERE clientRequestId = :clientRequestId LIMIT 1")
    suspend fun getMessageByClientRequestId(clientRequestId: String): MessengerMessageEntity?

    @Query("SELECT * FROM messenger_messages WHERE serverId = :serverId LIMIT 1")
    suspend fun getMessageByServerId(serverId: String): MessengerMessageEntity?

    @Query("SELECT * FROM messenger_messages WHERE deliveryStatus = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingMessages(): List<MessengerMessageEntity>

    @Query("UPDATE messenger_messages SET deliveryStatus = :status, serverId = :serverId WHERE id = :id")
    suspend fun updateDeliveryStatusAndServerId(id: Long, status: String, serverId: String?)

    @Query("UPDATE messenger_messages SET deliveryStatus = :status WHERE id = :id")
    suspend fun updateDeliveryStatus(id: Long, status: String)

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

    @Query("UPDATE messenger_messages SET text = :newText, editedAt = :editedTime WHERE serverId = :serverId")
    suspend fun editMessageByServerId(serverId: String, newText: String, editedTime: Long = System.currentTimeMillis())

    @Query("UPDATE messenger_messages SET deletedAt = :deletedTime, text = 'این پیام حذف شد' WHERE id = :id")
    suspend fun softDeleteMessage(id: Long, deletedTime: Long = System.currentTimeMillis())

    @Query("UPDATE messenger_messages SET deletedAt = :deletedTime, text = 'این پیام حذف شد' WHERE serverId = :serverId")
    suspend fun softDeleteMessageByServerId(serverId: String, deletedTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM messenger_messages WHERE id = :id")
    suspend fun deleteMessagePermanently(id: Long)

    @Query("DELETE FROM messenger_messages WHERE conversationId = :convId")
    suspend fun deleteAllMessagesInConversation(convId: String)
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY name ASC")
    suspend fun getAllGroups(): List<GroupEntity>

    @Query("SELECT * FROM groups WHERE conversationId = :convId LIMIT 1")
    suspend fun getGroupByConversationId(convId: String): GroupEntity?

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteGroup(id: String)

    // Members
    @Query("SELECT * FROM conversation_members WHERE conversationId = :convId")
    suspend fun getMembersForConversation(convId: String): List<ConversationMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: ConversationMemberEntity): Long

    @Query("DELETE FROM conversation_members WHERE conversationId = :convId AND userId = :userId")
    suspend fun removeMember(convId: String, userId: String)

    @Query("UPDATE conversation_members SET role = :newRole WHERE conversationId = :convId AND userId = :userId")
    suspend fun updateMemberRole(convId: String, userId: String, newRole: String)

    // Permissions
    @Query("SELECT * FROM group_permissions WHERE groupId = :groupId LIMIT 1")
    suspend fun getGroupPermissions(groupId: String): GroupPermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermissions(perms: GroupPermissionEntity)

    @Update
    suspend fun updatePermissions(perms: GroupPermissionEntity)
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY name ASC")
    suspend fun getAllChannels(): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE isSubscribed = 1 ORDER BY name ASC")
    suspend fun getSubscribedChannels(): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE conversationId = :convId LIMIT 1")
    suspend fun getChannelByConversationId(convId: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE id = :channelId LIMIT 1")
    suspend fun getChannelById(channelId: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE username = :username LIMIT 1")
    suspend fun getChannelByUsername(username: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Query("UPDATE channels SET isSubscribed = :subscribed, subscriberCount = subscriberCount + (CASE WHEN :subscribed = 1 THEN 1 ELSE -1 END) WHERE id = :channelId")
    suspend fun setSubscribed(channelId: String, subscribed: Boolean)

    @Query("DELETE FROM channels WHERE id = :id")
    suspend fun deleteChannel(id: String)

    // Channel Posts
    @Query("SELECT * FROM channel_posts WHERE channelId = :channelId ORDER BY createdAt DESC")
    suspend fun getPostsForChannel(channelId: String): List<ChannelPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ChannelPostEntity)

    @Update
    suspend fun updatePost(post: ChannelPostEntity)

    @Query("DELETE FROM channel_posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("UPDATE channel_posts SET reactionCount = reactionCount + 1 WHERE id = :postId")
    suspend fun incrementReaction(postId: String)
}

@Dao
interface ReactionDao {
    @Query("SELECT * FROM message_reactions WHERE messageId = :messageId")
    suspend fun getReactionsForMessage(messageId: String): List<MessageReactionEntity>

    @Query("SELECT * FROM message_reactions WHERE messageId = :messageId AND userId = :userId AND reaction = :reaction LIMIT 1")
    suspend fun getReaction(messageId: String, userId: String, reaction: String): MessageReactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: MessageReactionEntity): Long

    @Query("DELETE FROM message_reactions WHERE messageId = :messageId AND userId = :userId AND reaction = :reaction")
    suspend fun deleteReaction(messageId: String, userId: String, reaction: String)
}

@Dao
interface ModerationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedUser(blocked: BlockedUserEntity): Long

    @Query("DELETE FROM blocked_users WHERE userId = :userId")
    suspend fun removeBlockedUser(userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_users WHERE userId = :userId)")
    suspend fun isUserBlocked(userId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    suspend fun getAllReports(): List<ReportEntity>
}
