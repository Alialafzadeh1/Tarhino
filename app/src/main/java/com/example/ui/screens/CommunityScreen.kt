package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CommunityPostEntity
import com.example.domain.model.AppLanguage
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
fun CommunityScreen(
    currentLanguage: AppLanguage,
    posts: List<CommunityPostEntity>,
    onToggleLike: (CommunityPostEntity) -> Unit,
    onPublishPost: (String, String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var activeTab by remember { mutableStateOf("discover") } // "discover", "groups", "channels"
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (currentLanguage == AppLanguage.PERSIAN) "جامعه خلاق طرحی نو" else "Tarhi Noo Community",
                                color = PrimaryGold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.PERSIAN) "اشتراک‌گذاری تجربیات، پرامپت‌ها و گروه‌های طراحی" else "Discover prompts and creative showcases",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                onPublishPost("خلق جدید در استودیو با پرامپت هوش مصنوعی طرحی نو", "Futuristic Persian calligraphy concept with cinematic rim lighting.")
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (currentLanguage == AppLanguage.PERSIAN) "پست شما در جامعه منتشر شد" else "Post published"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Post", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLanguage == AppLanguage.PERSIAN) "انتشار اثر" else "Publish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard)
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        listOf(
                            "discover" to if (currentLanguage == AppLanguage.PERSIAN) "کاوش آثار" else "Discover",
                            "groups" to if (currentLanguage == AppLanguage.PERSIAN) "گروه‌ها" else "Groups",
                            "channels" to if (currentLanguage == AppLanguage.PERSIAN) "کانال‌ها" else "Channels"
                        ).forEach { (tabKey, label) ->
                            val isSelected = activeTab == tabKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandGreen else Color.Transparent)
                                    .clickable { activeTab = tabKey }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) SoftGold else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Tab content
            if (activeTab == "discover") {
                items(posts) { post ->
                    CommunityPostCard(
                        post = post,
                        currentLanguage = currentLanguage,
                        onToggleLike = { onToggleLike(post) },
                        onCopyPrompt = {
                            clipboard.setText(AnnotatedString(post.promptText))
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "پرامپت کپی شد" else "Prompt copied"
                                )
                            }
                        }
                    )
                }
            } else if (activeTab == "groups") {
                item {
                    GroupItem("گروه تخصصی پرامپت‌نویسان فارسی", "تبادل تکنیک‌های پیشرفته نگاتیو پرامپت و مهندسی تصویر", "۱,۲۴۰ عضو")
                    GroupItem("استودیو طراحی مذهبی و آیینی", "پوستر، تایپوگرافی کوفی، کتیبه و سناریونویسی محرم", "۸۹۰ عضو")
                    GroupItem("طراحان هوش مصنوعی تبلیغاتی", "رندرهای استودیویی محصولات لوکس، عطر و خودرو", "۲,۱۰۰ عضو")
                }
            } else {
                item {
                    ChannelItem("کانال رسمی طرحینه مدیا", "جدیدترین پرامپت‌ها و آموزش‌های ویدیویی استودیو", "۱۲,۴۰۰ دنبال‌کننده")
                    ChannelItem("کانال سینمایی Veo & Video AI", "ایده‌های تیزر تبلیغاتی و پرامپت‌های ۶۰ فریم", "۸,۳۰۰ دنبال‌کننده")
                }
            }
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPostEntity,
    currentLanguage: AppLanguage,
    onToggleLike: () -> Unit,
    onCopyPrompt: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BrandGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = post.authorName.take(1), color = SoftGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = post.authorName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = post.authorHandle, color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = post.content, color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp)

            if (post.promptText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgDark)
                        .border(0.8.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = post.promptText, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 2)
                        IconButton(onClick = onCopyPrompt, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SoftGold, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleLike, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color(0xFFE53E3E) else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(text = "${post.likesCount}", color = TextMuted, fontSize = 11.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.commentsCount}", color = TextMuted, fontSize = 11.sp)
                }

                Icon(Icons.Default.Share, contentDescription = "Share", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun GroupItem(title: String, desc: String, members: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = members, color = SoftGold, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ChannelItem(title: String, desc: String, subscribers: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(text = title, color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = subscribers, color = TextMuted, fontSize = 10.sp)
        }
    }
}
