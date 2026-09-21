package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NotificationEntity
import com.example.domain.model.AppLanguage
import com.example.ui.components.EmptyState
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
fun NotificationsScreen(
    currentLanguage: AppLanguage,
    notifications: List<NotificationEntity>,
    onMarkAllAsRead: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "اعلان‌ها (Notifications)" else "Notifications",
                            color = PrimaryGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "پیام‌های سیستم، تکمیل وظایف هوش مصنوعی و پروژه‌ها" else "System and AI completion alerts",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (notifications.isNotEmpty()) {
                        Button(
                            onClick = {
                                onMarkAllAsRead()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (currentLanguage == AppLanguage.PERSIAN) "تمام اعلان‌ها خوانده شد" else "All marked as read"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = SoftGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Read", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLanguage == AppLanguage.PERSIAN) "خوانده شد" else "Mark Read", fontSize = 11.sp)
                        }
                    }
                }
            }

            if (notifications.isEmpty()) {
                item {
                    EmptyState(
                        title = if (currentLanguage == AppLanguage.PERSIAN) "هیچ اعلانی وجود ندارد" else "No notifications",
                        description = if (currentLanguage == AppLanguage.PERSIAN) "وقتی رندرهای هوش مصنوعی کامل شوند یا اطلاعیه جدیدی باشد، اینجا می‌بینید." else "AI updates will be delivered here."
                    )
                }
            } else {
                items(notifications) { item ->
                    NotificationCard(item)
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: NotificationEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(
                1.dp,
                if (!item.isRead) PrimaryGold.copy(alpha = 0.5f) else SurfaceCardBorder.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BrandGreen.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (item.type) {
                    "AI_COMPLETED" -> Icons.Default.AutoAwesome
                    "PROJECT_UPDATE" -> Icons.Default.Folder
                    else -> Icons.Default.Info
                }
                Icon(icon, contentDescription = item.type, tint = SoftGold, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = item.message, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}
