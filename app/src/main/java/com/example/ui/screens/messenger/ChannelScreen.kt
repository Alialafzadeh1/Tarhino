package com.example.ui.screens.messenger

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Share
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
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ChannelPostEntity
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
fun ChannelScreen(
    currentLanguage: AppLanguage,
    channel: ChannelEntity?,
    posts: List<ChannelPostEntity>,
    onBack: () -> Unit,
    onToggleSubscribe: (Boolean) -> Unit,
    onPublishPost: (text: String, promptText: String) -> Unit,
    onOpenPromptBuilder: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    val scope = rememberCoroutineScope()
    var isPublishing by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }
    var newPromptText by remember { mutableStateOf("") }

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
                        .background(Brush.linearGradient(listOf(BrandGreenLight, PrimaryGold))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Campaign, null, tint = BgDark, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel?.name ?: if (isFa) "کانال رسمی" else "Channel",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${channel?.username ?: "channel"} • ${channel?.subscriberCount ?: 1} ${if (isFa) "مشترک" else "subscribers"}",
                        color = PrimaryGold,
                        fontSize = 11.sp
                    )
                }

                // Subscribe Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (channel?.isSubscribed == true) SurfaceCard else BrandGreen)
                        .border(0.8.dp, if (channel?.isSubscribed == true) SurfaceCardBorder else SoftGold, RoundedCornerShape(8.dp))
                        .clickable {
                            onToggleSubscribe(channel?.isSubscribed != true)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (channel?.isSubscribed == true) (if (isFa) "عضو هستید" else "Joined") else (if (isFa) "عضویت" else "Join"),
                        color = PrimaryGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Channel Description
            if (channel?.description?.isNotBlank() == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = channel.description,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Post Publisher (Owner/Admin affordance)
            if (isPublishing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, PrimaryGold, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (isFa) "انتشار پست جدید در کانال" else "Publish New Channel Post",
                            color = PrimaryGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicTextField(
                            value = newPostText,
                            onValueChange = { newPostText = it },
                            textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                            cursorBrush = SolidColor(PrimaryGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgDark, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            decorationBox = { inner ->
                                if (newPostText.isEmpty()) {
                                    Text(if (isFa) "متن اطلاعیه، تحلیل یا معرفی پروژه..." else "Post content...", color = TextMuted, fontSize = 12.sp)
                                }
                                inner()
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicTextField(
                            value = newPromptText,
                            onValueChange = { newPromptText = it },
                            textStyle = TextStyle(color = SoftGold, fontSize = 12.sp),
                            cursorBrush = SolidColor(PrimaryGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgDark, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            decorationBox = { inner ->
                                if (newPromptText.isEmpty()) {
                                    Text(if (isFa) "پرامپت پیوست (اختیاری جهت معمار پرامپت)..." else "Attached prompt (optional)...", color = TextMuted, fontSize = 11.sp)
                                }
                                inner()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { isPublishing = false }) {
                                Text(if (isFa) "انصراف" else "Cancel", color = TextMuted, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandGreen)
                                    .clickable {
                                        if (newPostText.isNotBlank()) {
                                            onPublishPost(newPostText.trim(), newPromptText.trim())
                                            newPostText = ""
                                            newPromptText = ""
                                            isPublishing = false
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(if (isFa) "انتشار" else "Publish", color = PrimaryGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Posts List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    ChannelPostCard(
                        post = post,
                        isFa = isFa,
                        onOpenPromptBuilder = onOpenPromptBuilder
                    )
                }
            }

            // Bottom bar with publish button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "ارسال محتوا فقط برای مدیران کانال مجاز است" else "Only channel admins can post",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceGlass)
                        .border(0.6.dp, PrimaryGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { isPublishing = !isPublishing }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isFa) "+ پست جدید" else "+ New Post",
                        color = PrimaryGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelPostCard(
    post: ChannelPostEntity,
    isFa: Boolean,
    onOpenPromptBuilder: (String) -> Unit
) {
    val formattedTime = remember(post.createdAt) {
        val sdf = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault())
        sdf.format(Date(post.createdAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(0.8.dp, SurfaceCardBorder.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.authorName,
                    color = PrimaryGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedTime,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.text,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            if (post.promptText.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgDark)
                        .border(0.6.dp, PrimaryGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, null, tint = PrimaryGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFa) "الگوی پرامپت ضمیمه شده" else "Attached Prompt",
                                color = PrimaryGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = post.promptText,
                            color = SoftGold,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandGreen)
                                .clickable { onOpenPromptBuilder(post.promptText) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .align(Alignment.End)
                        ) {
                            Text(
                                text = if (isFa) "باز کردن در Prompt Builder" else "Open in Prompt Builder",
                                color = PrimaryGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer metrics: reactions & views
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.FavoriteBorder, null, tint = PrimaryGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.reactionCount}", color = TextSecondary, fontSize = 11.sp)
                }
                Text("${post.viewCount} ${if (isFa) "بازدید" else "views"}", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}
