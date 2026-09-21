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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PromptEntity
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
fun ExploreScreen(
    currentLanguage: AppLanguage,
    prompts: List<PromptEntity>,
    onToggleFavorite: (PromptEntity) -> Unit,
    onToggleSaved: (PromptEntity) -> Unit,
    onCopyPrompt: (PromptEntity) -> Unit,
    onUseInBuilder: (PromptEntity) -> Unit,
    onOptimizeWithAI: (PromptEntity) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedPromptDetail by remember { mutableStateOf<PromptEntity?>(null) }

    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val filters = if (currentLanguage == AppLanguage.PERSIAN) {
        listOf(
            "همه" to "All",
            "تصویر" to "IMAGE",
            "ویدیو" to "VIDEO",
            "ویژه" to "Featured",
            "رایگان" to "Free",
            "پریمیوم" to "Premium",
            "محبوب‌ترین" to "Popular"
        )
    } else {
        listOf(
            "All" to "All",
            "Images" to "IMAGE",
            "Videos" to "VIDEO",
            "Featured" to "Featured",
            "Free" to "Free",
            "Premium" to "Premium",
            "Popular" to "Popular"
        )
    }

    val categories = if (currentLanguage == AppLanguage.PERSIAN) {
        listOf(
            "همه دسته‌ها" to "all",
            "تبلیغاتی و محصول" to "commercial",
            "سینمایی و دراماتیک" to "cinematic",
            "سایبرپانک و آینده" to "cyberpunk",
            "هنری و مفهومی" to "artistic",
            "معماری و فضا" to "architecture"
        )
    } else {
        listOf(
            "All" to "all",
            "Commercial" to "commercial",
            "Cinematic" to "cinematic",
            "Cyberpunk" to "cyberpunk",
            "Artistic" to "artistic",
            "Architecture" to "architecture"
        )
    }

    val filteredPrompts = prompts.filter { p ->
        val matchesQuery = searchQuery.isBlank() ||
                p.title.contains(searchQuery, ignoreCase = true) ||
                p.prompt.contains(searchQuery, ignoreCase = true) ||
                p.tags.contains(searchQuery, ignoreCase = true) ||
                p.description.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "IMAGE" -> p.type == "IMAGE"
            "VIDEO" -> p.type == "VIDEO"
            "Featured" -> p.isFeatured
            "Free" -> !p.isPremium
            "Premium" -> p.isPremium
            "Popular" -> p.copyCount > 20
            else -> true
        }

        val matchesCategory = if (selectedCategory == "all") true else p.categoryId.equals(selectedCategory, ignoreCase = true)

        matchesQuery && matchesFilter && matchesCategory
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Search Bar Header
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "کاوش پرامپت‌ها و الهام خلاقانه" else "Explore Prompts & Inspiration",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "جستجو در میان صدها پرامپت برتر و سناریوهای آماده" else "Search across hundreds of top prompts and creative briefs",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SoftGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = if (currentLanguage == AppLanguage.PERSIAN)
                                            "جستجو (مثلاً: عطر لوکس، سایبرپانک، پوستر عاشورا)..."
                                        else
                                            "Search prompts, styles, keywords...",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                                    cursorBrush = SolidColor(PrimaryGold),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Filters Row (Horizontal Scroll)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { (label, key) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BrandGreen else SurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) SoftGold else SurfaceCardBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilter = key }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) SoftGold else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Category Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (label, key) ->
                        val isSelected = selectedCategory == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SurfaceCardBorder else Color.Transparent)
                                .border(1.dp, if (isSelected) PrimaryGold else SurfaceCardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = key }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) PrimaryGold else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Prompts List
            if (filteredPrompts.isEmpty()) {
                item {
                    EmptyState(
                        title = if (currentLanguage == AppLanguage.PERSIAN) "پرامپتی یافت نشد" else "No prompts found",
                        description = if (currentLanguage == AppLanguage.PERSIAN) "عبارت دیگری را جستجو کنید یا فیلترها را تغییر دهید." else "Try another search keyword or clear filters.",
                        buttonText = if (currentLanguage == AppLanguage.PERSIAN) "پاک‌سازی فیلترها" else "Reset Filters",
                        onButtonClick = {
                            searchQuery = ""
                            selectedFilter = "All"
                            selectedCategory = "all"
                        }
                    )
                }
            } else {
                items(filteredPrompts) { prompt ->
                    PromptExploreCard(
                        prompt = prompt,
                        currentLanguage = currentLanguage,
                        onClick = { selectedPromptDetail = prompt },
                        onCopy = {
                            clipboard.setText(AnnotatedString(prompt.prompt))
                            onCopyPrompt(prompt)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "پرامپت با موفقیت کپی شد" else "Prompt copied"
                                )
                            }
                        },
                        onToggleFavorite = { onToggleFavorite(prompt) },
                        onToggleSaved = { onToggleSaved(prompt) }
                    )
                }
            }
        }

        // Prompt Detail Modal (Specification #12)
        selectedPromptDetail?.let { prompt ->
            PromptDetailDialog(
                prompt = prompt,
                currentLanguage = currentLanguage,
                onDismiss = { selectedPromptDetail = null },
                onCopy = {
                    clipboard.setText(AnnotatedString(prompt.prompt))
                    onCopyPrompt(prompt)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (currentLanguage == AppLanguage.PERSIAN) "پرامپت کپی شد" else "Prompt copied"
                        )
                    }
                },
                onToggleFavorite = { onToggleFavorite(prompt) },
                onToggleSaved = { onToggleSaved(prompt) },
                onUseInBuilder = {
                    selectedPromptDetail = null
                    onUseInBuilder(prompt)
                },
                onOptimizeWithAI = {
                    selectedPromptDetail = null
                    onOptimizeWithAI(prompt)
                }
            )
        }
    }
}

@Composable
private fun PromptExploreCard(
    prompt: PromptEntity,
    currentLanguage: AppLanguage,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleSaved: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (prompt.type == "VIDEO") Color(0xFF2C194D) else BrandGreen.copy(alpha = 0.6f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = prompt.type,
                            color = SoftGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (prompt.style.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${prompt.style}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (prompt.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (prompt.isFavorite) Color(0xFFE53E3E) else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onToggleSaved, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (prompt.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (prompt.isSaved) PrimaryGold else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = SoftGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = prompt.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = prompt.prompt,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            if (prompt.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    prompt.tags.split(",").take(4).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceCardBorder.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "#${tag.trim()}", color = SoftGold, fontSize = 9.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ارائه‌شده توسط ${prompt.author}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "${prompt.copyCount} کپی • ${prompt.favoriteCount} ستاره",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PromptDetailDialog(
    prompt: PromptEntity,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleSaved: () -> Unit,
    onUseInBuilder: () -> Unit,
    onOptimizeWithAI: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp)) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandGreen)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = prompt.type, color = SoftGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = prompt.title,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = prompt.description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prompt Box
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "متن پرامپت:" else "Prompt text:",
                        color = PrimaryGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgDark)
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = prompt.prompt, color = TextPrimary, fontSize = 12.sp, lineHeight = 18.sp)
                    }

                    if (prompt.negativePrompt.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "نگاتیو پرامپت:" else "Negative Prompt:",
                            color = Color(0xFFE53E3E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BgDark)
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = prompt.negativePrompt, color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Parameters Pills
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (prompt.style.isNotBlank()) ParamBadge("Style", prompt.style)
                        if (prompt.lighting.isNotBlank()) ParamBadge("Light", prompt.lighting)
                        if (prompt.camera.isNotBlank()) ParamBadge("Camera", prompt.camera)
                        if (prompt.lens.isNotBlank()) ParamBadge("Lens", prompt.lens)
                        if (prompt.material.isNotBlank()) ParamBadge("Material", prompt.material)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons (Specification #12)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onCopy,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = SoftGold),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (currentLanguage == AppLanguage.PERSIAN) "کپی" else "Copy", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onUseInBuilder,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Builder", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (currentLanguage == AppLanguage.PERSIAN) "استفاده در معمار" else "In Architect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onOptimizeWithAI,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardBorder, contentColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "بهینه‌سازی با هوش مصنوعی" else "Optimize with AI", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParamBadge(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BrandGreen.copy(alpha = 0.3f))
            .border(0.8.dp, SurfaceCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = "$label: $value", color = SoftGold, fontSize = 10.sp)
    }
}
