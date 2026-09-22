package com.example.ui.screens.messenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MessengerConversationEntity
import com.example.data.local.entity.MessengerMessageEntity
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrivateChatScreen(
    currentLanguage: AppLanguage,
    conversation: MessengerConversationEntity?,
    messages: List<MessengerMessageEntity>,
    onBack: () -> Unit,
    onSendMessage: (text: String, replyToId: String?, replyToText: String?, replyToSender: String?) -> Unit,
    onForwardMessage: (MessengerMessageEntity) -> Unit,
    onEditMessage: (Long, String) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onPinMessage: (Long, Boolean) -> Unit,
    onReaction: (String, String) -> Unit,
    onOpenPromptBuilder: (String) -> Unit,
    onOpenNavaStudio: () -> Unit,
    onReport: (String, String) -> Unit,
    isUserTyping: Boolean = false,
    snackbarHostState: SnackbarHostState
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<MessengerMessageEntity?>(null) }
    var editingMessage by remember { mutableStateOf<MessengerMessageEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val displayMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) messages
        else messages.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .border(0.6.dp, SurfaceCardBorder.copy(alpha = 0.3f), RoundedCornerShape(0.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = PrimaryGold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (conversation?.type == "AI") Brush.linearGradient(listOf(BrandGreenGlow, PrimaryGold))
                            else Brush.linearGradient(listOf(BrandGreen, BrandGreenLight))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (conversation?.type == "AI") Icons.Filled.AutoAwesome else Icons.Filled.Person,
                        contentDescription = null,
                        tint = if (conversation?.type == "AI") BgDark else PrimaryGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = conversation?.title ?: if (isFa) "گفت‌وگو" else "Chat",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isUserTyping) {
                            if (isFa) "در حال نوشتن..." else "typing..."
                        } else {
                            when (conversation?.type) {
                                "AI" -> if (isFa) "هوش مصنوعی آماده پاسخگویی" else "AI Active"
                                "GROUP" -> if (isFa) "گروه اعضا" else "Group"
                                "CHANNEL" -> if (isFa) "کانال رسمی" else "Official Channel"
                                else -> if (isFa) "آنلاین" else "Online"
                            }
                        },
                        color = if (isUserTyping) PrimaryGold else if (conversation?.type == "AI") PrimaryGold else AccentCyan,
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = { isSearchActive = !isSearchActive }) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = "جستجو",
                        tint = if (isSearchActive) PrimaryGold else TextSecondary
                    )
                }
            }

            // In-Chat Search Bar
            AnimatedVisibility(visible = isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgDark, RoundedCornerShape(8.dp))
                            .border(0.8.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = if (isFa) "جستجوی پیام‌ها در این گفت‌وگو..." else "Search messages in chat...",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayMessages, key = { it.id }) { msg ->
                    val isMe = msg.senderDisplayName == "من" || msg.senderId == "current_user" || msg.senderId == "0" || msg.senderId.isBlank()
                    MessageBubbleItem(
                        message = msg,
                        isMe = isMe,
                        isFa = isFa,
                        onReply = { replyingToMessage = msg },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(msg.text))
                            scope.launch { snackbarHostState.showSnackbar(if (isFa) "پیام کپی شد" else "Copied") }
                        },
                        onForward = { onForwardMessage(msg) },
                        onEdit = {
                            editingMessage = msg
                            inputText = msg.text
                        },
                        onDelete = { onDeleteMessage(msg.id) },
                        onTogglePin = { onPinMessage(msg.id, !msg.isPinned) },
                        onReaction = { emoji -> onReaction(msg.serverId ?: msg.id.toString(), emoji) },
                        onOpenPromptBuilder = { p -> onOpenPromptBuilder(p) },
                        onOpenNavaStudio = onOpenNavaStudio,
                        onReport = { onReport("MESSAGE", msg.serverId ?: msg.id.toString()) }
                    )
                }
            }

            // Quoted Reply Preview
            AnimatedVisibility(visible = replyingToMessage != null) {
                replyingToMessage?.let { rep ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark)
                            .border(width = 0.6.dp, color = SurfaceCardBorder)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(28.dp)
                                        .background(PrimaryGold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isFa) "پاسخ به ${rep.senderDisplayName}" else "Replying to ${rep.senderDisplayName}",
                                        color = PrimaryGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = rep.text,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(onClick = { replyingToMessage = null }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "انصراف",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Edit Preview
            AnimatedVisibility(visible = editingMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isFa) "در حال ویرایش پیام..." else "Editing message...",
                            color = PrimaryGold,
                            fontSize = 12.sp
                        )
                        IconButton(onClick = {
                            editingMessage = null
                            inputText = ""
                        }) {
                            Icon(Icons.Filled.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Composer Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .border(0.6.dp, SurfaceCardBorder.copy(alpha = 0.3f), RoundedCornerShape(0.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment action
                IconButton(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isFa) "پیوست محلی آماده است (تصویر، ویدیو، فایل پرامپت)" else "Local attachment ready"
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.AttachFile,
                        contentDescription = "پیوست",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // AI Shortcut button in composer
                IconButton(
                    onClick = {
                        if (!inputText.startsWith("@TarhiNooAI ")) {
                            inputText = "@TarhiNooAI " + inputText
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "هوش مصنوعی",
                        tint = PrimaryGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(BgDark, RoundedCornerShape(20.dp))
                        .border(0.8.dp, SurfaceCardBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = if (isFa) "پیام بنویسید یا @TarhiNooAI را صدا کنید..." else "Write a message or mention @TarhiNooAI...",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                            inner()
                        }
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send or Voice
                if (inputText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val textToSend = inputText.trim()
                            if (editingMessage != null) {
                                onEditMessage(editingMessage!!.id, textToSend)
                                editingMessage = null
                            } else {
                                onSendMessage(
                                    textToSend,
                                    replyingToMessage?.serverId ?: replyingToMessage?.id?.toString(),
                                    replyingToMessage?.text?.take(40),
                                    replyingToMessage?.senderDisplayName
                                )
                                replyingToMessage = null
                            }
                            inputText = ""
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BrandGreen)
                            .border(1.dp, SoftGold, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "ارسال",
                            tint = PrimaryGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isFa) "ضبط صدا (آماده‌سازی لایه محلی)" else "Voice recording local layer"
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "صدا",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubbleItem(
    message: MessengerMessageEntity,
    isMe: Boolean,
    isFa: Boolean,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onForward: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onReaction: (String) -> Unit,
    onOpenPromptBuilder: (String) -> Unit,
    onOpenNavaStudio: () -> Unit,
    onReport: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val formattedTime = remember(message.createdAt) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.createdAt))
    }

    val bubbleColor = when {
        message.isAiGenerated -> Brush.linearGradient(listOf(BrandGreen.copy(alpha = 0.9f), BrandGreenGlow.copy(alpha = 0.6f)))
        isMe -> Brush.linearGradient(listOf(BrandGreen, BrandGreenLight))
        else -> Brush.linearGradient(listOf(SurfaceDark, SurfaceCard))
    }

    val borderColor = when {
        message.isAiGenerated -> PrimaryGold.copy(alpha = 0.6f)
        isMe -> SoftGold.copy(alpha = 0.4f)
        else -> SurfaceCardBorder.copy(alpha = 0.3f)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 0.8.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .clickable { showMenu = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                // Header if Group or Forward
                if (message.forwardedFromSenderName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Forward,
                            contentDescription = null,
                            tint = PrimaryGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFa) "هدایت‌شده از ${message.forwardedFromSenderName}" else "Forwarded from ${message.forwardedFromSenderName}",
                            color = PrimaryGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Quoted Preview if Reply
                if (message.replyToText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(BgDark.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(20.dp)
                                    .background(PrimaryGold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSenderName ?: "کاربر",
                                    color = PrimaryGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = message.replyToText,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // AI Badge if AI message
                if (message.isAiGenerated) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "طرحی نو AI — پاسخ معمار خلاق",
                            color = PrimaryGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Main Message Text
                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )

                // Bridge to Prompt Builder / Nava Studio if action prompt exists
                if (message.aiActionPrompt != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark.copy(alpha = 0.8f))
                            .border(0.6.dp, PrimaryGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = message.aiActionPrompt,
                                color = SoftGold,
                                fontSize = 11.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BrandGreen)
                                        .border(0.6.dp, PrimaryGold, RoundedCornerShape(6.dp))
                                        .clickable { onOpenPromptBuilder(message.aiActionPrompt) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.AutoAwesome, null, tint = PrimaryGold, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isFa) "باز کردن در Prompt Builder" else "Prompt Builder", color = PrimaryGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceGlass)
                                        .border(0.6.dp, AccentCyan, RoundedCornerShape(6.dp))
                                        .clickable { onOpenNavaStudio() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Brush, null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isFa) "ناو استودیو" else "Nava Studio", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer: Timestamp + Delivery State + Pin
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (message.isPinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            tint = PrimaryGold,
                            modifier = Modifier.size(11.dp).padding(end = 4.dp)
                        )
                    }
                    if (message.editedAt != null) {
                        Text(
                            text = if (isFa) "ویرایش‌شده" else "edited",
                            color = TextMuted,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Sent",
                            tint = if (message.deliveryStatus == "READ") AccentCyan else PrimaryGold,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Context Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(SurfaceCard)
            ) {
                // Quick Reactions Row
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("❤️", "👍", "🔥", "😂", "😍", "😮").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clickable {
                                    showMenu = false
                                    onReaction(emoji)
                                }
                                .padding(4.dp)
                        )
                    }
                }
                HorizontalDivider(color = SurfaceCardBorder)
                DropdownMenuItem(
                    text = { Text(if (isFa) "پاسخ" else "Reply", color = TextPrimary) },
                    onClick = { showMenu = false; onReply() },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, null, tint = PrimaryGold) }
                )
                DropdownMenuItem(
                    text = { Text(if (isFa) "کپی متن" else "Copy", color = TextPrimary) },
                    onClick = { showMenu = false; onCopy() },
                    leadingIcon = { Icon(Icons.Filled.ContentCopy, null, tint = PrimaryGold) }
                )
                DropdownMenuItem(
                    text = { Text(if (isFa) "هدایت پیام (Forward)" else "Forward", color = TextPrimary) },
                    onClick = { showMenu = false; onForward() },
                    leadingIcon = { Icon(Icons.Filled.Forward, null, tint = PrimaryGold) }
                )
                DropdownMenuItem(
                    text = { Text(if (message.isPinned) (if (isFa) "برداشتن سنجاق" else "Unpin") else (if (isFa) "سنجاق پیام" else "Pin"), color = TextPrimary) },
                    onClick = { showMenu = false; onTogglePin() },
                    leadingIcon = { Icon(Icons.Filled.PushPin, null, tint = PrimaryGold) }
                )
                if (isMe) {
                    DropdownMenuItem(
                        text = { Text(if (isFa) "ویرایش" else "Edit", color = TextPrimary) },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Filled.Edit, null, tint = PrimaryGold) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isFa) "حذف پیام" else "Delete", color = Color(0xFFE53E3E)) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFE53E3E)) }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(if (isFa) "گزارش تخلف" else "Report", color = Color(0xFFE53E3E)) },
                        onClick = { showMenu = false; onReport() },
                        leadingIcon = { Icon(Icons.Filled.Warning, null, tint = Color(0xFFE53E3E)) }
                    )
                }
            }
        }
    }
}
