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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HistoryEntity
import com.example.domain.model.AppLanguage
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    currentLanguage: AppLanguage,
    historyList: List<HistoryEntity>,
    onClearAll: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    // Grouping by date: Today, Yesterday, Earlier
    val now = System.currentTimeMillis()
    val oneDay = 24 * 60 * 60 * 1000L
    val todayList = historyList.filter { now - it.timestamp < oneDay }
    val yesterdayList = historyList.filter { now - it.timestamp in oneDay..(2 * oneDay) }
    val earlierList = historyList.filter { now - it.timestamp > 2 * oneDay }

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
                            text = if (currentLanguage == AppLanguage.PERSIAN) "تاریخچه فعالیت‌ها (History)" else "Activity History",
                            color = PrimaryGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "گزارش تعاملات هوش مصنوعی، کپی‌ها و ویرایش‌ها" else "Audit log of AI generations and edits",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (historyList.isNotEmpty()) {
                        Button(
                            onClick = {
                                onClearAll()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (currentLanguage == AppLanguage.PERSIAN) "تاریخچه پاک‌سازی شد" else "History cleared"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = ErrorRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ClearAll, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLanguage == AppLanguage.PERSIAN) "پاک‌سازی" else "Clear", fontSize = 11.sp)
                        }
                    }
                }
            }

            if (historyList.isEmpty()) {
                item {
                    EmptyState(
                        title = if (currentLanguage == AppLanguage.PERSIAN) "تاریخچه‌ای ثبت نشده است" else "No history recorded yet",
                        description = if (currentLanguage == AppLanguage.PERSIAN)
                            "هر پرامپت کپی‌شده، پروژه ساخته‌شده یا ویرایشی در ناو استودیو به طور خودکار در این بخش ثبت می‌شود."
                        else
                            "Copied prompts and studio edits will appear here."
                    )
                }
            } else {
                if (todayList.isNotEmpty()) {
                    item { GroupHeader(if (currentLanguage == AppLanguage.PERSIAN) "امروز" else "Today") }
                    items(todayList) { entry -> HistoryItemRow(entry, onDelete = { onDeleteEntry(entry.id) }) }
                }

                if (yesterdayList.isNotEmpty()) {
                    item { GroupHeader(if (currentLanguage == AppLanguage.PERSIAN) "دیروز" else "Yesterday") }
                    items(yesterdayList) { entry -> HistoryItemRow(entry, onDelete = { onDeleteEntry(entry.id) }) }
                }

                if (earlierList.isNotEmpty()) {
                    item { GroupHeader(if (currentLanguage == AppLanguage.PERSIAN) "روزهای گذشته" else "Earlier") }
                    items(earlierList) { entry -> HistoryItemRow(entry, onDelete = { onDeleteEntry(entry.id) }) }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String) {
    Text(
        text = title,
        color = SoftGold,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun HistoryItemRow(
    entry: HistoryEntity,
    onDelete: () -> Unit
) {
    val icon: ImageVector = when (entry.type) {
        "PROMPT" -> Icons.Default.Bookmark
        "CHAT" -> Icons.Default.Chat
        "PROJECT" -> Icons.Default.Folder
        "IMAGE" -> Icons.Default.Image
        "VIDEO" -> Icons.Default.Movie
        "STUDIO_EDIT" -> Icons.Default.Brush
        else -> Icons.Default.History
    }

    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entry.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(BrandGreen.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = entry.type, tint = SoftGold, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = entry.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "${entry.detail} • $timeStr", color = TextMuted, fontSize = 11.sp)
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(14.dp))
            }
        }
    }
}
