package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AICoreState
import com.example.domain.model.AppLanguage
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TarhiNooTopBar(
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    aiState: AICoreState = AICoreState.IDLE,
    onNavigate: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand & Identity
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onNavigate("home") }
        ) {
            TarhiNooAICore(state = aiState, size = 36.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "طرحی نو" else "Tarhi Noo",
                        color = PrimaryGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandGreen)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI v2",
                            color = SoftGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "رسانه هنری طرحینه مدیا" else "Tarhineh Media",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Top actions: Language switch & Menu
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Language pill button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .clickable { onToggleLanguage() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = SoftGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "فا" else "EN",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Notifications Quick Icon
            IconButton(onClick = { onNavigate("notifications") }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Top-right Menu button
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = SoftGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(SurfaceCard)
                        .width(220.dp)
                ) {
                    MenuItemRow(Icons.Default.Bookmark, if (currentLanguage == AppLanguage.PERSIAN) "پرامپت‌های من" else "My Prompts") {
                        menuExpanded = false
                        onNavigate("my_prompts")
                    }
                    MenuItemRow(Icons.Default.Favorite, if (currentLanguage == AppLanguage.PERSIAN) "علاقه‌مندی‌ها" else "Favorites") {
                        menuExpanded = false
                        onNavigate("favorites")
                    }
                    MenuItemRow(Icons.Default.History, if (currentLanguage == AppLanguage.PERSIAN) "تاریخچه فعالیت" else "History") {
                        menuExpanded = false
                        onNavigate("history")
                    }
                    MenuItemRow(Icons.Default.Folder, if (currentLanguage == AppLanguage.PERSIAN) "پروژه‌ها" else "Projects") {
                        menuExpanded = false
                        onNavigate("projects")
                    }
                    MenuItemRow(Icons.Default.Widgets, if (currentLanguage == AppLanguage.PERSIAN) "مخزن دارایی‌ها (Asset Vault)" else "Asset Vault") {
                        menuExpanded = false
                        onNavigate("asset_vault")
                    }
                    MenuItemRow(Icons.Default.Brush, if (currentLanguage == AppLanguage.PERSIAN) "ناو استودیو (Nava Studio)" else "Nava Studio") {
                        menuExpanded = false
                        onNavigate("nava_studio")
                    }
                    MenuItemRow(Icons.Default.Diamond, if (currentLanguage == AppLanguage.PERSIAN) "اشتراک و تعرفه‌ها" else "Subscription") {
                        menuExpanded = false
                        onNavigate("subscription")
                    }
                    HorizontalDivider(color = BrandGreen.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                    MenuItemRow(Icons.Default.Security, if (currentLanguage == AppLanguage.PERSIAN) "پنل مدیریت (Admin)" else "Admin Panel") {
                        menuExpanded = false
                        onNavigate("admin")
                    }
                    MenuItemRow(Icons.Default.Settings, if (currentLanguage == AppLanguage.PERSIAN) "تنظیمات" else "Settings") {
                        menuExpanded = false
                        onNavigate("settings")
                    }
                    MenuItemRow(Icons.Default.Info, if (currentLanguage == AppLanguage.PERSIAN) "درباره طرحینه مدیا" else "About Tarhineh") {
                        menuExpanded = false
                        onNavigate("about")
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = SoftGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = label, color = TextPrimary, fontSize = 13.sp)
            }
        },
        onClick = onClick
    )
}
