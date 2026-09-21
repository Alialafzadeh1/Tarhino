package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.domain.model.AICoreState
import com.example.domain.model.AppLanguage
import com.example.ui.components.TarhiNooAICore
import com.example.ui.theme.BgDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AIChatScreen(
    currentLanguage: AppLanguage,
    conversations: List<ConversationEntity>,
    activeConversationId: Long,
    messages: List<MessageEntity>,
    isAiThinking: Boolean,
    onSelectConversation: (Long) -> Unit,
    onNewConversation: () -> Unit,
    onSendMessage: (String) -> Unit,
    onCreateProjectFromChat: (title: String, content: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val quickQuestions = if (currentLanguage == AppLanguage.PERSIAN) {
        listOf(
            "برای یک عطر لوکس یک تبلیغ بساز",
            "پوستر مفهومی عاشورایی با خط کوفی",
            "پرامپت عکاسی خودرو در شب بارانی",
            "ایده سناریوی تیزر تبلیغاتی استودیو"
        )
    } else {
        listOf(
            "Create a luxury perfume campaign",
            "Ashura conceptual poster with Kufic calligraphy",
            "Car photography prompt in rainy night",
            "Studio advertising teaser script"
        )
    }

    LaunchedEffect(messages.size, isAiThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Conversations selector top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandGreen)
                    .clickable { onNewConversation() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat", tint = SoftGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentLanguage == AppLanguage.PERSIAN) "گفتگوی جدید" else "New Chat", color = SoftGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                conversations.forEach { conv ->
                    val isSelected = conv.id == activeConversationId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceCardBorder else Color.Transparent)
                            .border(1.dp, if (isSelected) PrimaryGold else SurfaceCardBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onSelectConversation(conv.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = conv.title,
                            color = if (isSelected) PrimaryGold else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        TarhiNooAICore(size = 32.dp, modifier = Modifier.padding(end = 8.dp, top = 4.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            )
                            .background(if (isUser) BrandGreen else SurfaceCard)
                            .border(
                                1.dp,
                                if (isUser) SoftGold.copy(alpha = 0.4f) else SurfaceCardBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        if (!isUser) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.PERSIAN) "هوش مصنوعی طرحی نو" else "Tarhi Noo AI",
                                    color = PrimaryGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(msg.content))
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (currentLanguage == AppLanguage.PERSIAN) "پیام کپی شد" else "Copied"
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SoftGold, modifier = Modifier.size(14.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            onCreateProjectFromChat("ایده استودیو طرحی نو", msg.content)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (currentLanguage == AppLanguage.PERSIAN) "پروژه ایجاد شد" else "Project created"
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Folder, contentDescription = "To Project", tint = SoftGold, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = msg.content,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            if (isAiThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TarhiNooAICore(state = AICoreState.THINKING, size = 32.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "طرحی نو در حال تحلیل و طراحی پاسخ خلاقانه است..." else "Tarhi Noo AI is creating your response...",
                            color = SoftGold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Quick Suggestions Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickQuestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                        .clickable { onSendMessage(suggestion) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(text = suggestion, color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Bottom Chat Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgDark)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "پیام خود را بنویسید..." else "Ask Tarhi Noo AI...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                if (inputText.isNotBlank() && !isAiThinking) {
                    IconButton(
                        onClick = {
                            val text = inputText
                            inputText = ""
                            onSendMessage(text)
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PrimaryGold)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = BgDark, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
