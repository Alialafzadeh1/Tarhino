package com.example.data.repository

import com.example.data.local.AppDatabase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * Clean Room-based repository providing reactive data access for Tarhi Noo Messenger,
 * covering Users, Conversations, Messages, Groups, Channels, Reactions, Attachments, and Moderation.
 */
class MessengerRepository(private val db: AppDatabase) {

    // --- Users & Contacts ---
    val allActiveUsers: Flow<List<UserEntity>> = db.messengerUserDao().getAllActiveUsers()
    val contacts: Flow<List<UserEntity>> = db.messengerUserDao().getContacts()
    val blockedUsers: Flow<List<UserEntity>> = db.messengerUserDao().getBlockedUsers()

    suspend fun getUserById(userId: Long): UserEntity? = withContext(Dispatchers.IO) {
        db.messengerUserDao().getUserById(userId)
    }

    suspend fun getUserByUsername(username: String): UserEntity? = withContext(Dispatchers.IO) {
        val clean = username.removePrefix("@").trim().lowercase()
        db.messengerUserDao().getUserByUsername(clean)
    }

    fun searchUsers(query: String): Flow<List<UserEntity>> {
        val clean = query.removePrefix("@").trim()
        return db.messengerUserDao().searchUsers(clean)
    }

    suspend fun insertOrUpdateUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        val cleanUser = user.copy(username = user.username.removePrefix("@").trim().lowercase())
        db.messengerUserDao().insertUser(cleanUser)
    }

    suspend fun toggleContact(userId: Long, isContact: Boolean) = withContext(Dispatchers.IO) {
        db.messengerUserDao().setUserContact(userId, isContact)
    }

    // --- Conversations ---
    val allConversations: Flow<List<MessengerConversationEntity>> = db.messengerConversationDao().getAllConversations()
    val totalUnreadCount: Flow<Int?> = db.messengerConversationDao().getTotalUnreadCount()

    fun getConversationsByType(type: String): Flow<List<MessengerConversationEntity>> =
        db.messengerConversationDao().getConversationsByType(type)

    fun getArchivedConversations(): Flow<List<MessengerConversationEntity>> =
        db.messengerConversationDao().getArchivedConversations()

    suspend fun getConversationById(id: Long): MessengerConversationEntity? = withContext(Dispatchers.IO) {
        db.messengerConversationDao().getConversationById(id)
    }

    suspend fun getOrCreatePrivateConversation(user: UserEntity): Long = withContext(Dispatchers.IO) {
        val existing = db.messengerConversationDao().getPrivateConversationWithUser(user.id)
        if (existing != null) {
            existing.id
        } else {
            val newConv = MessengerConversationEntity(
                type = "PRIVATE",
                title = user.displayName,
                avatarUrl = user.avatarUrl,
                directUserId = user.id,
                lastMessageText = "گفتگو آغاز شد",
                lastMessageSenderName = user.displayName
            )
            val convId = db.messengerConversationDao().insertConversation(newConv)
            // Add conversation members
            db.groupDao().insertMember(
                ConversationMemberEntity(conversationId = convId, userId = 0L, role = "MEMBER")
            )
            db.groupDao().insertMember(
                ConversationMemberEntity(conversationId = convId, userId = user.id, role = "MEMBER")
            )
            convId
        }
    }

    suspend fun createGroupConversation(
        name: String,
        description: String,
        selectedUserIds: List<Long>
    ): Long = withContext(Dispatchers.IO) {
        val conv = MessengerConversationEntity(
            type = "GROUP",
            title = name,
            description = description,
            lastMessageText = "گروه ایجاد شد",
            lastMessageSenderName = "سیستم"
        )
        val convId = db.messengerConversationDao().insertConversation(conv)
        val group = GroupEntity(
            conversationId = convId,
            name = name,
            description = description,
            ownerId = 0L,
            memberCount = selectedUserIds.size + 1,
            inviteCode = "TRH-${System.currentTimeMillis() % 100000}"
        )
        val groupId = db.groupDao().insertGroup(group)
        // Group permissions
        db.groupDao().insertPermissions(GroupPermissionEntity(groupId = groupId))
        // Add current user as OWNER
        db.groupDao().insertMember(
            ConversationMemberEntity(conversationId = convId, userId = 0L, role = "OWNER")
        )
        // Add selected members
        selectedUserIds.forEach { uid ->
            db.groupDao().insertMember(
                ConversationMemberEntity(conversationId = convId, userId = uid, role = "MEMBER")
            )
        }
        convId
    }

    suspend fun createChannel(
        name: String,
        username: String,
        description: String
    ): Long = withContext(Dispatchers.IO) {
        val cleanUsername = username.removePrefix("@").trim().lowercase()
        val conv = MessengerConversationEntity(
            type = "CHANNEL",
            title = name,
            description = description,
            lastMessageText = "کانال راه‌اندازی شد",
            lastMessageSenderName = name
        )
        val convId = db.messengerConversationDao().insertConversation(conv)
        val channel = ChannelEntity(
            conversationId = convId,
            name = name,
            username = cleanUsername,
            description = description,
            ownerId = 0L,
            subscriberCount = 1,
            isSubscribed = true,
            inviteLink = "https://t.me/tarhinoo/$cleanUsername"
        )
        db.channelDao().insertChannel(channel)
        convId
    }

    suspend fun setPinned(convId: Long, pinned: Boolean) = withContext(Dispatchers.IO) {
        db.messengerConversationDao().setPinned(convId, pinned)
    }

    suspend fun setMuted(convId: Long, muted: Boolean) = withContext(Dispatchers.IO) {
        db.messengerConversationDao().setMuted(convId, muted)
    }

    suspend fun setArchived(convId: Long, archived: Boolean) = withContext(Dispatchers.IO) {
        db.messengerConversationDao().setArchived(convId, archived)
    }

    suspend fun markAsRead(convId: Long) = withContext(Dispatchers.IO) {
        db.messengerConversationDao().markAsRead(convId)
    }

    suspend fun deleteConversation(convId: Long) = withContext(Dispatchers.IO) {
        db.messengerMessageDao().deleteAllMessagesInConversation(convId)
        db.messengerConversationDao().deleteConversationById(convId)
    }

    // --- Messages ---
    fun getMessagesForConversation(convId: Long): Flow<List<MessengerMessageEntity>> =
        db.messengerMessageDao().getMessagesForConversation(convId)

    suspend fun getMessageById(messageId: Long): MessengerMessageEntity? = withContext(Dispatchers.IO) {
        db.messengerMessageDao().getMessageById(messageId)
    }

    fun getPinnedMessages(convId: Long): Flow<List<MessengerMessageEntity>> =
        db.messengerMessageDao().getPinnedMessages(convId)

    fun searchMessagesInConversation(convId: Long, query: String): Flow<List<MessengerMessageEntity>> =
        db.messengerMessageDao().searchMessagesInConversation(convId, query)

    fun globalSearchMessages(query: String): Flow<List<MessengerMessageEntity>> =
        db.messengerMessageDao().globalSearchMessages(query)

    suspend fun sendMessage(
        conversationId: Long,
        text: String,
        senderId: Long = 0L,
        senderDisplayName: String = "من",
        messageType: String = "TEXT",
        replyToMessageId: Long? = null,
        replyToText: String? = null,
        replyToSenderName: String? = null,
        forwardedFromMessageId: Long? = null,
        forwardedFromSenderName: String? = null,
        isAiGenerated: Boolean = false,
        aiActionPrompt: String? = null,
        aiTargetModule: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val message = MessengerMessageEntity(
            conversationId = conversationId,
            senderId = senderId,
            senderDisplayName = senderDisplayName,
            text = text,
            messageType = messageType,
            deliveryStatus = if (senderId == 0L) "SENT" else "READ",
            replyToMessageId = replyToMessageId,
            replyToText = replyToText,
            replyToSenderName = replyToSenderName,
            forwardedFromMessageId = forwardedFromMessageId,
            forwardedFromSenderName = forwardedFromSenderName,
            isAiGenerated = isAiGenerated,
            aiActionPrompt = aiActionPrompt,
            aiTargetModule = aiTargetModule
        )
        val msgId = db.messengerMessageDao().insertMessage(message)
        val unreadDelta = if (senderId != 0L) 1 else 0
        db.messengerConversationDao().updateLastMessage(
            id = conversationId,
            lastText = text.take(60),
            sender = senderDisplayName,
            time = message.createdAt,
            unreadDelta = unreadDelta
        )
        msgId
    }

    suspend fun forwardMessage(
        sourceMessage: MessengerMessageEntity,
        targetConversationId: Long
    ): Long = withContext(Dispatchers.IO) {
        val forwardMsg = MessengerMessageEntity(
            conversationId = targetConversationId,
            senderId = 0L,
            senderDisplayName = "من",
            text = sourceMessage.text,
            messageType = sourceMessage.messageType,
            deliveryStatus = "SENT",
            forwardedFromMessageId = sourceMessage.id,
            forwardedFromSenderName = sourceMessage.senderDisplayName.ifBlank { "کاربر" },
            isAiGenerated = sourceMessage.isAiGenerated,
            aiActionPrompt = sourceMessage.aiActionPrompt,
            aiTargetModule = sourceMessage.aiTargetModule
        )
        val id = db.messengerMessageDao().insertMessage(forwardMsg)
        db.messengerConversationDao().updateLastMessage(
            id = targetConversationId,
            lastText = "پیام هدایت شده: ${sourceMessage.text.take(40)}",
            sender = "من",
            time = forwardMsg.createdAt,
            unreadDelta = 0
        )
        id
    }

    suspend fun editMessage(messageId: Long, newText: String) = withContext(Dispatchers.IO) {
        db.messengerMessageDao().editMessageText(messageId, newText)
    }

    suspend fun setMessagePinned(messageId: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        db.messengerMessageDao().setMessagePinned(messageId, isPinned)
    }

    suspend fun deleteMessage(messageId: Long, forEveryone: Boolean = true) = withContext(Dispatchers.IO) {
        if (forEveryone) {
            db.messengerMessageDao().softDeleteMessage(messageId)
        } else {
            db.messengerMessageDao().deleteMessagePermanently(messageId)
        }
    }

    // --- Reactions ---
    fun getReactionsForMessage(messageId: Long): Flow<List<MessageReactionEntity>> =
        db.reactionDao().getReactionsForMessage(messageId)

    suspend fun toggleReaction(messageId: Long, userId: Long = 0L, reactionEmoji: String) = withContext(Dispatchers.IO) {
        val existing = db.reactionDao().getReaction(messageId, userId, reactionEmoji)
        if (existing != null) {
            db.reactionDao().deleteReaction(messageId, userId, reactionEmoji)
        } else {
            db.reactionDao().insertReaction(
                MessageReactionEntity(messageId = messageId, userId = userId, reaction = reactionEmoji)
            )
        }
    }

    // --- Groups & Channels ---
    val allGroups: Flow<List<GroupEntity>> = db.groupDao().getAllGroups()
    val allChannels: Flow<List<ChannelEntity>> = db.channelDao().getAllChannels()
    val subscribedChannels: Flow<List<ChannelEntity>> = db.channelDao().getSubscribedChannels()

    fun getMembersForConversation(convId: Long): Flow<List<ConversationMemberEntity>> =
        db.groupDao().getMembersForConversation(convId)

    suspend fun getGroupByConversationId(convId: Long): GroupEntity? = withContext(Dispatchers.IO) {
        db.groupDao().getGroupByConversationId(convId)
    }

    suspend fun getChannelByConversationId(convId: Long): ChannelEntity? = withContext(Dispatchers.IO) {
        db.channelDao().getChannelByConversationId(convId)
    }

    fun getPostsForChannel(channelId: Long): Flow<List<ChannelPostEntity>> =
        db.channelDao().getPostsForChannel(channelId)

    suspend fun publishChannelPost(
        channelId: Long,
        text: String,
        mediaUrl: String = "",
        mediaType: String = "TEXT",
        promptText: String = ""
    ): Long = withContext(Dispatchers.IO) {
        db.channelDao().insertPost(
            ChannelPostEntity(
                channelId = channelId,
                authorId = 0L,
                authorName = "طرحی نو مدیا",
                text = text,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                promptText = promptText
            )
        )
    }

    suspend fun toggleChannelSubscription(channelId: Long, isSubscribed: Boolean) = withContext(Dispatchers.IO) {
        db.channelDao().setSubscribed(channelId, isSubscribed)
    }

    // --- Moderation & Reporting ---
    suspend fun blockUser(userId: Long, reason: String = "") = withContext(Dispatchers.IO) {
        db.messengerUserDao().setUserBlocked(userId, true)
        db.moderationDao().insertBlockedUser(BlockedUserEntity(userId = userId, reason = reason))
    }

    suspend fun unblockUser(userId: Long) = withContext(Dispatchers.IO) {
        db.messengerUserDao().setUserBlocked(userId, false)
        db.moderationDao().removeBlockedUser(userId)
    }

    suspend fun report(targetType: String, targetId: Long, reason: String, details: String = ""): Long = withContext(Dispatchers.IO) {
        db.moderationDao().insertReport(
            ReportEntity(
                targetType = targetType,
                targetId = targetId,
                reasonCategory = reason,
                details = details
            )
        )
    }

    // --- Initial Seed Data for Messenger (Real, clean local seed) ---
    suspend fun seedInitialMessengerDataIfNeeded() = withContext(Dispatchers.IO) {
        if (db.messengerUserDao().getUserCount() == 0) {
            val initialUsers = listOf(
                UserEntity(
                    id = 1L,
                    username = "tarhinoo_ai",
                    displayName = "هوش مصنوعی طرحی نو",
                    bio = "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.» معمار پرامپت، مشاور هنری و پردازش خلاق",
                    isOnline = true,
                    isVerified = true,
                    isContact = true
                ),
                UserEntity(
                    id = 2L,
                    username = "reza_art",
                    displayName = "رضا مهدوی",
                    bio = "طراح هویت بصری و کاربر حرفه‌ای معمار پرامپت طرحی نو",
                    isOnline = true,
                    isVerified = true,
                    isContact = true
                ),
                UserEntity(
                    id = 3L,
                    username = "sara_cinema",
                    displayName = "سارا علوی",
                    bio = "فیلمساز و کارگردان هوش مصنوعی - تخصص در ویدیوهای سینمایی Veo و Sora",
                    isOnline = false,
                    isVerified = false,
                    isContact = true
                ),
                UserEntity(
                    id = 4L,
                    username = "tarhineh_studio",
                    displayName = "استودیو طرحینه",
                    bio = "کانون نوآوری هنر رسانه‌ای طرحینه مدیا",
                    isOnline = true,
                    isVerified = true,
                    isContact = false
                )
            )
            db.messengerUserDao().insertUsers(initialUsers)

            // 1. Conversation with Tarhi Noo AI
            val aiConv = MessengerConversationEntity(
                id = 1L,
                type = "AI",
                title = "هوش مصنوعی طرحی نو (@TarhiNooAI)",
                avatarUrl = "",
                directUserId = 1L,
                unreadCount = 1,
                lastMessageText = "درود! آماده ساخت پرامپت، پوستر یا سناریوی خلاق هستم.",
                lastMessageSenderName = "هوش مصنوعی طرحی نو"
            )
            db.messengerConversationDao().insertConversation(aiConv)
            db.messengerMessageDao().insertMessage(
                MessengerMessageEntity(
                    conversationId = 1L,
                    senderId = 1L,
                    senderDisplayName = "هوش مصنوعی طرحی نو",
                    text = "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\n\nمی‌توانید مستقیماً در این چت، با من گفتگو کنید یا در هر گروه و کانال از منوی هوش مصنوعی درخواست معمار پرامپت یا ناو استودیو داشته باشید.",
                    messageType = "AI_RESULT",
                    isAiGenerated = true,
                    aiActionPrompt = "Cinematic Iranian architectural pavilion at golden hour, traditional turquoise tiles with volumetric sunlight, 8K render",
                    aiTargetModule = "PROMPT_BUILDER"
                )
            )

            // 2. Private chat with Reza Mahdavi
            val rezaConv = MessengerConversationEntity(
                id = 2L,
                type = "PRIVATE",
                title = "رضا مهدوی",
                avatarUrl = "",
                directUserId = 2L,
                unreadCount = 0,
                lastMessageText = "سلام علی جان، پرامپت پوستر محرم رو در ناو استودیو تست کردی؟",
                lastMessageSenderName = "رضا مهدوی"
            )
            db.messengerConversationDao().insertConversation(rezaConv)
            db.messengerMessageDao().insertMessage(
                MessengerMessageEntity(
                    conversationId = 2L,
                    senderId = 2L,
                    senderDisplayName = "رضا مهدوی",
                    text = "سلام علی جان، پرامپت پوستر محرم رو در ناو استودیو تست کردی؟ کیفیت نورپردازی حجمی فوق‌العاده شده بود.",
                    messageType = "TEXT"
                )
            )

            // 3. Creative Group
            val groupConv = MessengerConversationEntity(
                id = 3L,
                type = "GROUP",
                title = "انجمن طراحان هوش مصنوعی طرحینه",
                avatarUrl = "",
                unreadCount = 2,
                lastMessageText = "سارا علوی: جدیدترین مقایسه فلوکس و میدجرنی رو گذاشتم",
                lastMessageSenderName = "سارا علوی"
            )
            db.messengerConversationDao().insertConversation(groupConv)
            val groupId = db.groupDao().insertGroup(
                GroupEntity(
                    id = 1L,
                    conversationId = 3L,
                    name = "انجمن طراحان هوش مصنوعی طرحینه",
                    description = "گفتگوی تخصصی حول معمار پرامپت، ناو استودیو، موشن و نوآوری‌های هنر دیجیتال",
                    memberCount = 38,
                    inviteCode = "TRH-GRP-2026"
                )
            )
            db.groupDao().insertPermissions(GroupPermissionEntity(groupId = groupId))
            db.messengerMessageDao().insertMessage(
                MessengerMessageEntity(
                    conversationId = 3L,
                    senderId = 3L,
                    senderDisplayName = "سارا علوی",
                    text = "دوستان عزیز، با پرامپت معمار طرحی نو، نتیجه ویدیوی 60fps با دوربین Dolly Zoom فوق‌العاده نرم شد!",
                    messageType = "TEXT"
                )
            )

            // 4. Official Channel
            val channelConv = MessengerConversationEntity(
                id = 4L,
                type = "CHANNEL",
                title = "رسانه هنری طرحینه مدیا",
                avatarUrl = "",
                unreadCount = 0,
                lastMessageText = "انتشار نسخه جدید معمار پرامپت طرحی نو با پشتیبانی کامل ناو استودیو",
                lastMessageSenderName = "طرحینه مدیا"
            )
            db.messengerConversationDao().insertConversation(channelConv)
            val channelId = db.channelDao().insertChannel(
                ChannelEntity(
                    id = 1L,
                    conversationId = 4L,
                    name = "رسانه هنری طرحینه مدیا",
                    username = "tarhineh_media",
                    description = "کانال رسمی اطلاع‌رسانی، انتشار الگوهای خلاق و تکنیک‌های برتر هوش مصنوعی",
                    subscriberCount = 1420,
                    isSubscribed = true,
                    inviteLink = "https://tarhineh.media/c/tarhineh_media"
                )
            )
            db.channelDao().insertPost(
                ChannelPostEntity(
                    channelId = channelId,
                    authorId = 0L,
                    authorName = "طرحینه مدیا",
                    text = "«طرحی نو — رسانه هنری طرحینه مدیا»\n\nپیام‌رسان مستقل و یکپارچه طرحی نو راه‌اندازی شد. اکنون می‌توانید پروژه‌ها، پرامپت‌ها و لایه‌های ناو استودیو را مستقیماً با هنرمندان به اشتراک بگذارید.",
                    promptText = "Futuristic neon Persian calligraphy floating over obsidian water, emerald glow, 8k luxury style",
                    reactionCount = 89,
                    viewCount = 650
                )
            )
        }
    }
}
