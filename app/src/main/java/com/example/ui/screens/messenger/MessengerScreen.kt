package com.example.ui.screens.messenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.MessengerConversationEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.AppLanguage
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BgDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenGlow
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MessengerTab {
    CHATS, GROUPS, CHANNELS, CONTACTS
}

@Composable
fun MessengerScreen(
    currentLanguage: AppLanguage,
    conversations: List<MessengerConversationEntity>,
    groups: List<GroupEntity>,
    channels: List<ChannelEntity>,
    contacts: List<UserEntity>,
    onOpenConversation: (Long) -> Unit,
    onOpenChannel: (Long) -> Unit,
    onOpenUserProfile: (Long) -> Unit,
    onNewMessageClick: () -> Unit,
    onTogglePin: (Long, Boolean) -> Unit,
    onToggleMute: (Long, Boolean) -> Unit,
    onToggleArchive: (Long, Boolean) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onMarkAsRead: (Long) -> Unit,
    onOpenBackendConfig: () -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    var selectedTab by remember { mutableStateOf(MessengerTab.CHATS) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val filteredConversations = remember(conversations, searchQuery, selectedTab) {
        val base = when (selectedTab) {
            MessengerTab.CHATS -> conversations.filter { it.type == "PRIVATE" || it.type == "AI" }
            MessengerTab.GROUPS -> conversations.filter { it.type == "GROUP" }
            MessengerTab.CHANNELS -> conversations.filter { it.type == "CHANNEL" }
            MessengerTab.CONTACTS -> conversations
        }
        if (searchQuery.isBlank()) base
        else base.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.lastMessageText.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(BrandGreen, BrandGreenGlow))
                            )
                            .border(1.dp, SurfaceCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = null,
                            tint = PrimaryGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isFa) "پیام‌ها" else "Messages",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isFa) "پیام‌رسان یکپارچه طرحی نو" else "Tarhi Noo Messenger Hub",
                            color = PrimaryGold,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenBackendConfig
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudQueue,
                            contentDescription = "Backend Status",
                            tint = PrimaryGold
                        )
                    }
                    IconButton(
                        onClick = { isSearchActive = !isSearchActive }
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = "جستجو",
                            tint = if (isSearchActive) PrimaryGold else TextSecondary
                        )
                    }
                }
            }

            // Search Bar (Expandable)
            AnimatedVisibility(visible = isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                            cursorBrush = SolidColor(PrimaryGold),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = if (isFa) "جستجو در پیام‌ها، کاربران یا گروه‌ها..." else "Search chats, users, groups...",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            // Segmented Tabs: چت‌ها | گروه‌ها | کانال‌ها | مخاطبین
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(SurfaceDark, RoundedCornerShape(16.dp))
                    .border(0.8.dp, SurfaceCardBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MessengerTabItem(
                    label = if (isFa) "چت‌ها" else "Chats",
                    isSelected = selectedTab == MessengerTab.CHATS,
                    icon = Icons.Filled.Chat,
                    onClick = { selectedTab = MessengerTab.CHATS },
                    modifier = Modifier.weight(1f)
                )
                MessengerTabItem(
                    label = if (isFa) "گروه‌ها" else "Groups",
                    isSelected = selectedTab == MessengerTab.GROUPS,
                    icon = Icons.Filled.Group,
                    onClick = { selectedTab = MessengerTab.GROUPS },
                    modifier = Modifier.weight(1f)
                )
                MessengerTabItem(
                    label = if (isFa) "کانال‌ها" else "Channels",
                    isSelected = selectedTab == MessengerTab.CHANNELS,
                    icon = Icons.Filled.Campaign,
                    onClick = { selectedTab = MessengerTab.CHANNELS },
                    modifier = Modifier.weight(1f)
                )
                MessengerTabItem(
                    label = if (isFa) "مخاطبین" else "Contacts",
                    isSelected = selectedTab == MessengerTab.CONTACTS,
                    icon = Icons.Filled.People,
                    onClick = { selectedTab = MessengerTab.CONTACTS },
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Content
            when (selectedTab) {
                MessengerTab.CHATS -> {
                    if (filteredConversations.isEmpty()) {
                        EmptyMessengerState(
                            isFa = isFa,
                            title = if (isFa) "هنوز گفت‌وگویی شروع نکرده‌اید" else "No conversations yet",
                            subtitle = if (isFa) "با دوستان، طراحان یا هوش مصنوعی طرحی نو چت کنید." else "Start chatting with friends, designers, or Tarhi Noo AI.",
                            actionLabel = if (isFa) "شروع گفت‌وگو" else "Start Chat",
                            onAction = onNewMessageClick
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredConversations, key = { it.id }) { conv ->
                                ConversationListItem(
                                    conversation = conv,
                                    isFa = isFa,
                                    onClick = {
                                        onMarkAsRead(conv.id)
                                        onOpenConversation(conv.id)
                                    },
                                    onTogglePin = { onTogglePin(conv.id, !conv.isPinned) },
                                    onToggleMute = { onToggleMute(conv.id, !conv.isMuted) },
                                    onToggleArchive = { onToggleArchive(conv.id, !conv.isArchived) },
                                    onDelete = { onDeleteConversation(conv.id) },
                                    onMarkRead = { onMarkAsRead(conv.id) }
                                )
                            }
                        }
                    }
                }

                MessengerTab.GROUPS -> {
                    if (filteredConversations.isEmpty()) {
                        EmptyMessengerState(
                            isFa = isFa,
                            title = if (isFa) "هنوز عضو گروهی نیستید" else "No groups yet",
                            subtitle = if (isFa) "یک گروه تخصصی بسازید یا به انجمن‌های طراحی بپیوندید." else "Create a group or join design communities.",
                            actionLabel = if (isFa) "ساخت گروه جدید" else "Create Group",
                            onAction = onNewMessageClick
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredConversations, key = { it.id }) { conv ->
                                ConversationListItem(
                                    conversation = conv,
                                    isFa = isFa,
                                    onClick = {
                                        onMarkAsRead(conv.id)
                                        onOpenConversation(conv.id)
                                    },
                                    onTogglePin = { onTogglePin(conv.id, !conv.isPinned) },
                                    onToggleMute = { onToggleMute(conv.id, !conv.isMuted) },
                                    onToggleArchive = { onToggleArchive(conv.id, !conv.isArchived) },
                                    onDelete = { onDeleteConversation(conv.id) },
                                    onMarkRead = { onMarkAsRead(conv.id) }
                                )
                            }
                        }
                    }
                }

                MessengerTab.CHANNELS -> {
                    if (filteredConversations.isEmpty()) {
                        EmptyMessengerState(
                            isFa = isFa,
                            title = if (isFa) "هنوز کانالی دنبال نمی‌کنید" else "No channels followed",
                            subtitle = if (isFa) "کانال‌های رسمی طرحینه مدیا را برای دریافت پرامپت‌ها دنبال کنید." else "Follow official channels to get creative prompts.",
                            actionLabel = if (isFa) "ساخت کانال" else "Create Channel",
                            onAction = onNewMessageClick
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredConversations, key = { it.id }) { conv ->
                                ConversationListItem(
                                    conversation = conv,
                                    isFa = isFa,
                                    onClick = {
                                        onMarkAsRead(conv.id)
                                        onOpenConversation(conv.id)
                                    },
                                    onTogglePin = { onTogglePin(conv.id, !conv.isPinned) },
                                    onToggleMute = { onToggleMute(conv.id, !conv.isMuted) },
                                    onToggleArchive = { onToggleArchive(conv.id, !conv.isArchived) },
                                    onDelete = { onDeleteConversation(conv.id) },
                                    onMarkRead = { onMarkAsRead(conv.id) }
                                )
                            }
                        }
                    }
                }

                MessengerTab.CONTACTS -> {
                    val contactList = remember(contacts, searchQuery) {
                        if (searchQuery.isBlank()) contacts
                        else contacts.filter {
                            it.displayName.contains(searchQuery, ignoreCase = true) ||
                            it.username.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (contactList.isEmpty()) {
                        EmptyMessengerState(
                            isFa = isFa,
                            title = if (isFa) "مخاطبی یافت نشد" else "No contacts found",
                            subtitle = if (isFa) "با شناسه یا نام کاربری دوستان خود را پیدا کنید." else "Find designers by username.",
                            actionLabel = if (isFa) "شروع چت جدید" else "New Chat",
                            onAction = onNewMessageClick
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(contactList, key = { it.id }) { user ->
                                ContactRowItem(
                                    user = user,
                                    isFa = isFa,
                                    onClick = { onOpenUserProfile(user.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to start new conversation / group / channel
        FloatingActionButton(
            onClick = onNewMessageClick,
            containerColor = BrandGreen,
            contentColor = PrimaryGold,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .border(1.2.dp, SoftGold, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = if (isFa) "پیام جدید" else "New message",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun MessengerTabItem(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BrandGreen else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryGold else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isSelected) PrimaryGold else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ConversationListItem(
    conversation: MessengerConversationEntity,
    isFa: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    onMarkRead: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val formattedTime = remember(conversation.lastMessageTimestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(conversation.lastMessageTimestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (conversation.isPinned) SurfaceCard.copy(alpha = 0.95f) else SurfaceDark)
            .border(
                width = if (conversation.isPinned) 1.dp else 0.6.dp,
                color = if (conversation.isPinned) SurfaceCardBorder else SurfaceCardBorder.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with badges
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            when (conversation.type) {
                                "AI" -> Brush.linearGradient(listOf(BrandGreenGlow, PrimaryGold))
                                "GROUP" -> Brush.linearGradient(listOf(BrandGreen, AccentCyan))
                                "CHANNEL" -> Brush.linearGradient(listOf(BrandGreenLight, PrimaryGold))
                                else -> Brush.linearGradient(listOf(BrandGreen, BrandGreenLight))
                            }
                        )
                        .border(1.dp, SurfaceCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (conversation.type) {
                            "AI" -> Icons.Filled.AutoAwesome
                            "GROUP" -> Icons.Filled.Group
                            "CHANNEL" -> Icons.Filled.Campaign
                            else -> Icons.Filled.Person
                        },
                        contentDescription = null,
                        tint = if (conversation.type == "AI") BgDark else PrimaryGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // AI Indicator or Pinned badge
                if (conversation.isPinned) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(PrimaryGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = null,
                            tint = BgDark,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = conversation.title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (conversation.type == "AI") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrimaryGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI",
                                    color = PrimaryGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessageText.ifBlank { "..." },
                        color = if (conversation.unreadCount > 0) TextPrimary else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (conversation.isMuted) {
                            Icon(
                                imageVector = Icons.Filled.VolumeMute,
                                contentDescription = "Muted",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp).padding(end = 4.dp)
                            )
                        }

                        if (conversation.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(BrandGreenLight)
                                    .border(1.dp, PrimaryGold.copy(alpha = 0.6f), CircleShape)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conversation.unreadCount.toString(),
                                    color = PrimaryGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Context menu button
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "بیشتر",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isFa) "خوانده شده" else "Mark as read", color = TextPrimary) },
                                onClick = { showMenu = false; onMarkRead() },
                                leadingIcon = { Icon(Icons.Filled.Check, null, tint = PrimaryGold) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (conversation.isPinned) (if (isFa) "برداشتن سنجاق" else "Unpin") else (if (isFa) "سنجاق کردن" else "Pin"), color = TextPrimary) },
                                onClick = { showMenu = false; onTogglePin() },
                                leadingIcon = { Icon(Icons.Filled.PushPin, null, tint = PrimaryGold) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (conversation.isMuted) (if (isFa) "لغو بی‌صدا" else "Unmute") else (if (isFa) "بی‌صدا" else "Mute"), color = TextPrimary) },
                                onClick = { showMenu = false; onToggleMute() },
                                leadingIcon = { Icon(if (conversation.isMuted) Icons.Filled.Notifications else Icons.Filled.NotificationsOff, null, tint = PrimaryGold) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (conversation.isArchived) (if (isFa) "خروج از بایگانی" else "Unarchive") else (if (isFa) "بایگانی" else "Archive"), color = TextPrimary) },
                                onClick = { showMenu = false; onToggleArchive() },
                                leadingIcon = { Icon(Icons.Filled.Archive, null, tint = PrimaryGold) }
                            )
                            HorizontalDivider(color = SurfaceCardBorder)
                            DropdownMenuItem(
                                text = { Text(if (isFa) "حذف محلی گفت‌وگو" else "Delete Chat", color = Color(0xFFE53E3E)) },
                                onClick = { showMenu = false; onDelete() },
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFE53E3E)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactRowItem(
    user: UserEntity,
    isFa: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(0.6.dp, SurfaceCardBorder.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BrandGreen, BrandGreenLight)))
                    .border(1.dp, SurfaceCardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = PrimaryGold,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Verified",
                            tint = AccentCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "@${user.username}",
                    color = PrimaryGold,
                    fontSize = 12.sp
                )
                if (user.bio.isNotBlank()) {
                    Text(
                        text = user.bio,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (user.isOnline) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38A169))
                )
            }
        }
    }
}

@Composable
fun EmptyMessengerState(
    isFa: Boolean,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceGlass)
                .border(1.dp, SurfaceCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Chat,
                contentDescription = null,
                tint = PrimaryGold,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(BrandGreen)
                .border(1.dp, SoftGold, RoundedCornerShape(12.dp))
                .clickable { onAction() }
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = actionLabel,
                color = PrimaryGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
