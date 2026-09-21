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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Widgets
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AssetEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.AssetCategory
import com.example.ui.components.EmptyState
import com.example.ui.theme.BgDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AssetVaultScreen(
    currentLanguage: AppLanguage,
    assets: List<AssetEntity>,
    onAddAsset: (title: String, type: String) -> Unit,
    onDeleteAsset: (Long) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var selectedCategory by remember { mutableStateOf(AssetCategory.ALL) }
    val scope = rememberCoroutineScope()

    val filteredAssets = assets.filter { asset ->
        when (selectedCategory) {
            AssetCategory.ALL -> true
            AssetCategory.IMAGES -> asset.type == "IMAGE"
            AssetCategory.VIDEOS -> asset.type == "VIDEO"
            AssetCategory.PROMPTS -> asset.type == "PROMPT"
            AssetCategory.TEMPLATES -> asset.type == "TEMPLATE"
            AssetCategory.PRESETS -> asset.type == "PRESET"
            AssetCategory.PROJECTS -> asset.type == "PROJECT"
            AssetCategory.REFERENCES -> asset.type == "REFERENCE"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
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
                            text = if (currentLanguage == AppLanguage.PERSIAN) "مخزن دارایی‌ها (Asset Vault)" else "Asset Vault",
                            color = PrimaryGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "مدیریت تصاویر، ویدیوها، پریست‌ها و مراجع هنری" else "Central vault for references, assets, and outputs",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            onAddAsset("دارایی جدید استودیو", "IMAGE")
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "دارایی افزوده شد" else "Asset added"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "افزودن" else "Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Categories Filter Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssetCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BrandGreen else SurfaceCard)
                                .border(1.dp, if (isSelected) SoftGold else SurfaceCardBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.PERSIAN) cat.titleFa else cat.titleEn,
                                color = if (isSelected) SoftGold else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            if (filteredAssets.isEmpty()) {
                item {
                    EmptyState(
                        title = if (currentLanguage == AppLanguage.PERSIAN) "هنوز فایلی در این دسته ذخیره نشده است" else "Vault is currently empty",
                        description = if (currentLanguage == AppLanguage.PERSIAN)
                            "خروجی‌های تصویری و ویدیویی استودیو، پریست‌ها و پرامپت‌ها در این مخزن قرار می‌گیرند."
                        else
                            "Saved images, videos, and presets will be organized here.",
                        buttonText = if (currentLanguage == AppLanguage.PERSIAN) "+ ثبت دارایی نمونه" else "+ Add Sample Asset",
                        onButtonClick = {
                            onAddAsset("تصویر رندرینگ استودیویی عطر", "IMAGE")
                        }
                    )
                }
            } else {
                items(filteredAssets) { asset ->
                    AssetCardItem(
                        asset = asset,
                        currentLanguage = currentLanguage,
                        onDelete = {
                            onDeleteAsset(asset.id)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "دارایی حذف شد" else "Asset removed"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetCardItem(
    asset: AssetEntity,
    currentLanguage: AppLanguage,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandGreen.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (asset.type) {
                        "IMAGE" -> Icons.Default.Image
                        "VIDEO" -> Icons.Default.Movie
                        "PROMPT" -> Icons.Default.Bookmark
                        else -> Icons.Default.Widgets
                    }
                    Icon(icon, contentDescription = asset.type, tint = SoftGold, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = asset.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "${asset.type} • ${asset.category}", color = TextMuted, fontSize = 10.sp)
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
