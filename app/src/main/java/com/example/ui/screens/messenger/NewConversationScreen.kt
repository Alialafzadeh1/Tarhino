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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class NewConversationAction {
    LIST, NEW_GROUP, NEW_CHANNEL
}

@Composable
fun NewConversationScreen(
    currentLanguage: AppLanguage,
    users: List<UserEntity>,
    onBack: () -> Unit,
    onStartPrivateChat: (UserEntity) -> Unit,
    onCreateGroup: (name: String, desc: String, selectedUserIds: List<Long>) -> Unit,
    onCreateChannel: (name: String, username: String, desc: String) -> Unit,
    onStartAIChat: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    val scope = rememberCoroutineScope()

    var currentView by remember { mutableStateOf(NewConversationAction.LIST) }
    var searchQuery by remember { mutableStateOf("") }

    // Group creation form state
    var groupName by remember { mutableStateOf("") }
    var groupDesc by remember { mutableStateOf("") }
    var selectedGroupUsers by remember { mutableStateOf(setOf<Long>()) }

    // Channel creation form state
    var channelName by remember { mutableStateOf("") }
    var channelUsername by remember { mutableStateOf("") }
    var channelDesc by remember { mutableStateOf("") }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentView != NewConversationAction.LIST) {
                        currentView = NewConversationAction.LIST
                    } else {
                        onBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = PrimaryGold
                    )
                }

                Text(
                    text = when (currentView) {
                        NewConversationAction.LIST -> if (isFa) "گفت‌وگوی جدید" else "New Message"
                        NewConversationAction.NEW_GROUP -> if (isFa) "ساخت گروه جدید" else "New Group"
                        NewConversationAction.NEW_CHANNEL -> if (isFa) "ساخت کانال جدید" else "New Channel"
                    },
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            when (currentView) {
                NewConversationAction.LIST -> {
                    // Quick Action Cards
                    Column(modifier = Modifier.padding(16.dp)) {
                        // AI Chat option
                        QuickActionCard(
                            title = if (isFa) "چت با هوش مصنوعی طرحی نو (@TarhiNooAI)" else "Chat with Tarhi Noo AI",
                            subtitle = if (isFa) "مشاور هنری، ساخت پرامپت سینمایی و تحلیل استودیو" else "Creative prompt architect & studio advisor",
                            icon = Icons.Filled.AutoAwesome,
                            isAI = true,
                            onClick = onStartAIChat
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // New Group
                        QuickActionCard(
                            title = if (isFa) "ساخت گروه جدید" else "Create New Group",
                            subtitle = if (isFa) "گفتگوی گروهی با طراحان و اعضا" else "Group discussion with creators",
                            icon = Icons.Filled.Group,
                            onClick = { currentView = NewConversationAction.NEW_GROUP }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // New Channel
                        QuickActionCard(
                            title = if (isFa) "ساخت کانال جدید" else "Create New Channel",
                            subtitle = if (isFa) "انتشار یکطرفه پرامپت‌ها و پروژه‌ها" else "Broadcast prompts & projects",
                            icon = Icons.Filled.Campaign,
                            onClick = { currentView = NewConversationAction.NEW_CHANNEL }
                        )
                    }

                    // Search input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                            .border(0.8.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                                cursorBrush = SolidColor(PrimaryGold),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = if (isFa) "جستجوی کاربر با نام یا @شناسه..." else "Search user by name or @handle...",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    Text(
                        text = if (isFa) "کاربران و مخاطبین" else "Users & Contacts",
                        color = PrimaryGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // User list
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->
                            ContactRowItem(
                                user = user,
                                isFa = isFa,
                                onClick = { onStartPrivateChat(user) }
                            )
                        }
                    }
                }

                NewConversationAction.NEW_GROUP -> {
                    // Group Creation Form
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (isFa) "اطلاعات گروه" else "Group Details",
                            color = PrimaryGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FormTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            placeholder = if (isFa) "نام گروه (الزامی)" else "Group Name"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FormTextField(
                            value = groupDesc,
                            onValueChange = { groupDesc = it },
                            placeholder = if (isFa) "توضیحات و زمینه فعالیت گروه" else "Group Description"
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isFa) "انتخاب اعضا (${selectedGroupUsers.size})" else "Select Members (${selectedGroupUsers.size})",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(users) { u ->
                                val isSelected = selectedGroupUsers.contains(u.id)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) BrandGreen.copy(alpha = 0.5f) else SurfaceDark)
                                        .border(
                                            0.6.dp,
                                            if (isSelected) PrimaryGold else SurfaceCardBorder.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedGroupUsers = if (isSelected) {
                                                selectedGroupUsers - u.id
                                            } else {
                                                selectedGroupUsers + u.id
                                            }
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(u.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("@${u.username}", color = PrimaryGold, fontSize = 12.sp)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Filled.Check, null, tint = PrimaryGold, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (groupName.isNotBlank()) BrandGreen else SurfaceDark)
                                .border(1.dp, if (groupName.isNotBlank()) SoftGold else SurfaceCardBorder, RoundedCornerShape(14.dp))
                                .clickable(enabled = groupName.isNotBlank()) {
                                    onCreateGroup(groupName.trim(), groupDesc.trim(), selectedGroupUsers.toList())
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isFa) "ایجاد گروه" else "Create Group",
                                color = if (groupName.isNotBlank()) PrimaryGold else TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                NewConversationAction.NEW_CHANNEL -> {
                    // Channel Creation Form
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (isFa) "اطلاعات کانال رسمی" else "Official Channel Details",
                            color = PrimaryGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FormTextField(
                            value = channelName,
                            onValueChange = { channelName = it },
                            placeholder = if (isFa) "نام کانال (مثلاً: استودیو تیزرسازی)" else "Channel Name"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FormTextField(
                            value = channelUsername,
                            onValueChange = { channelUsername = it },
                            placeholder = if (isFa) "شناسه کانال (انگلیسی، بدون @)" else "Channel username (e.g. teasers)"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FormTextField(
                            value = channelDesc,
                            onValueChange = { channelDesc = it },
                            placeholder = if (isFa) "توضیحات کانال برای مشترکین" else "Channel description"
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (channelName.isNotBlank() && channelUsername.isNotBlank()) BrandGreen else SurfaceDark)
                                .border(1.dp, if (channelName.isNotBlank() && channelUsername.isNotBlank()) SoftGold else SurfaceCardBorder, RoundedCornerShape(14.dp))
                                .clickable(enabled = channelName.isNotBlank() && channelUsername.isNotBlank()) {
                                    onCreateChannel(channelName.trim(), channelUsername.trim(), channelDesc.trim())
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isFa) "راه‌اندازی کانال" else "Launch Channel",
                                color = if (channelName.isNotBlank() && channelUsername.isNotBlank()) PrimaryGold else TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isAI: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isAI) BrandGreen.copy(alpha = 0.4f) else SurfaceDark)
            .border(
                0.8.dp,
                if (isAI) PrimaryGold else SurfaceCardBorder.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAI) Brush.linearGradient(listOf(BrandGreenGlow, PrimaryGold))
                        else Brush.linearGradient(listOf(BrandGreen, BrandGreenLight))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isAI) BgDark else PrimaryGold,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .border(0.8.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
            cursorBrush = SolidColor(PrimaryGold),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(text = placeholder, color = TextMuted, fontSize = 13.sp)
                }
                inner()
            }
        )
    }
}
