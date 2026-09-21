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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.SubscriptionTier
import com.example.ui.components.TarhiNooAICore
import com.example.ui.theme.BgDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ProfileAndSettingsScreen(
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    onNavigate: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var activeTier by remember { mutableStateOf(SubscriptionTier.PRO) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // User Profile Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BrandGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "User", tint = SoftGold, modifier = Modifier.size(30.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "کاربر استودیو طرحی نو", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrimaryGold)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = activeTier.name, color = BgDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "@tarhi_creator • حساب متصل", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }

        // Language & Localization Setting
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = SoftGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = if (currentLanguage == AppLanguage.PERSIAN) "زبان رابط کاربری" else "Interface Language", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = if (currentLanguage == AppLanguage.PERSIAN) "فارسی (RTL) / English (LTR)" else "Persian (RTL) / English (LTR)", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onToggleLanguage,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = SoftGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = if (currentLanguage == AppLanguage.PERSIAN) "تغییر به English" else "Switch to فارسی", fontSize = 11.sp)
                    }
                }
            }
        }

        // Subscription Tiers (Specification #27)
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Diamond, contentDescription = "Subscription", tint = PrimaryGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "پلن‌های اشتراک و سطوح دسترسی:" else "Subscription Plans:",
                        color = PrimaryGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SubscriptionTier.values().forEach { tier ->
                    val isCurrent = activeTier == tier
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCurrent) BrandGreen.copy(alpha = 0.4f) else SurfaceCard)
                            .border(1.dp, if (isCurrent) SoftGold else SurfaceCardBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                activeTier = tier
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (currentLanguage == AppLanguage.PERSIAN) "پلن به ${tier.titleFa} تغییر یافت" else "Plan switched to ${tier.titleEn}"
                                    )
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (currentLanguage == AppLanguage.PERSIAN) tier.titleFa else tier.titleEn,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(PrimaryGold)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "فعال", color = BgDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "روزانه ${tier.dailyAiLimit} درخواست هوش مصنوعی • تا ${tier.maxProjects} پروژه فعال",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = SoftGold, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // Admin Panel & About Tarhineh Media
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                // Admin Entry
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
                        .clickable { onNavigate("admin") }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = "Admin", tint = PrimaryGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = if (currentLanguage == AppLanguage.PERSIAN) "ورود به پنل مدیریت (Admin Panel)" else "Admin Panel Workspace", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Backend & Realtime Config Entry (Phase 9)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.dp, PrimaryGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { onNavigate("backend_config") }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = "Backend", tint = PrimaryGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.PERSIAN) "پیکربندی سرور و همگام‌سازی (Backend & Sync)" else "Backend & Sync Hub",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // About Tarhineh Media Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .border(1.dp, PrimaryGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TarhiNooAICore(size = 28.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "رسانه هنری طرحینه مدیا", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "«من هوش مصنوعی طرحی نو هستم؛ از رسانه هنری طرحینه مدیا.»\nطرحی نو، بستر جامع و پیشگام تولید محتوای هوش مصنوعی، پرامپت‌نویسی حرفه‌ای، سناریوسازی تبلیغاتی و ناو استودیو.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
