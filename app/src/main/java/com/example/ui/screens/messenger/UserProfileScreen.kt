package com.example.ui.screens.messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.domain.model.AppLanguage
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BgDark
import com.example.ui.theme.BrandGreen
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

@Composable
fun UserProfileScreen(
    currentLanguage: AppLanguage,
    user: UserEntity?,
    onBack: () -> Unit,
    onStartChat: () -> Unit,
    onToggleBlock: (Boolean) -> Unit,
    onReport: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    val scope = rememberCoroutineScope()

    var whoCanMessageMe by remember { mutableStateOf("همه اعضا") }
    var whoCanSeeOnline by remember { mutableStateOf("مخاطبین") }
    var allowFindMeByUsername by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
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
                Text(
                    text = if (isFa) "پروفایل کاربر" else "User Profile",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // User Identity Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(BrandGreen, BrandGreenLight)))
                        .border(2.dp, SoftGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = PrimaryGold, modifier = Modifier.size(44.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user?.displayName ?: "کاربر طرحی نو",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (user?.isVerified == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Filled.Check, null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = "@${user?.username ?: "user"}",
                    color = PrimaryGold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (user?.isOnline == true) (if (isFa) "آنلاین" else "Online") else (if (isFa) "آخرین بازدید اخیراً" else "Last seen recently"),
                    color = if (user?.isOnline == true) Color(0xFF38A169) else TextMuted,
                    fontSize = 12.sp
                )

                if (user?.bio?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = user.bio,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Send Message / Block / Report
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandGreen)
                            .border(1.dp, SoftGold, RoundedCornerShape(12.dp))
                            .clickable { onStartChat() }
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Chat, null, tint = PrimaryGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFa) "ارسال پیام" else "Send Message", color = PrimaryGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(color = SurfaceCardBorder, modifier = Modifier.padding(horizontal = 16.dp))

            // Privacy & Moderation section
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isFa) "تنظیمات حریم خصوصی و امنیت" else "Privacy & Moderation",
                    color = PrimaryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceDark)
                        .border(0.8.dp, SurfaceCardBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(if (isFa) "چه کسانی می‌توانند پیام دهند" else "Who can message me", color = TextPrimary, fontSize = 13.sp)
                                Text(whoCanMessageMe, color = TextMuted, fontSize = 11.sp)
                            }
                            Icon(Icons.Filled.Lock, null, tint = PrimaryGold, modifier = Modifier.size(16.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(if (isFa) "نمایش وضعیت آنلاین" else "Online status", color = TextPrimary, fontSize = 13.sp)
                                Text(whoCanSeeOnline, color = TextMuted, fontSize = 11.sp)
                            }
                            Icon(Icons.Filled.Lock, null, tint = PrimaryGold, modifier = Modifier.size(16.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(if (isFa) "یافتن با نام کاربری" else "Find by username", color = TextPrimary, fontSize = 13.sp)
                                Text(if (allowFindMeByUsername) "فعال" else "غیرفعال", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = allowFindMeByUsername,
                                onCheckedChange = { allowFindMeByUsername = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PrimaryGold,
                                    checkedTrackColor = BrandGreen
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Block & Report actions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(0.8.dp, Color(0xFFE53E3E).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onToggleBlock(user?.isBlocked != true) }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Block, null, tint = Color(0xFFE53E3E), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (user?.isBlocked == true) (if (isFa) "رفع مسدودیت کاربر" else "Unblock User") else (if (isFa) "مسدود کردن این کاربر" else "Block User"),
                            color = Color(0xFFE53E3E),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(0.8.dp, SurfaceCardBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { onReport() }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = PrimaryGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isFa) "گزارش تخلف کاربر به پشتیبانی طرحی نو" else "Report user to Tarhi Noo",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
