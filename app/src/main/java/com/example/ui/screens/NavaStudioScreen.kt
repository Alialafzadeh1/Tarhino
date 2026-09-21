package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.CanvasLayer
import com.example.domain.model.StudioTool
import com.example.ui.components.TarhiNooAICore
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BgDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenGlow
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun NavaStudioScreen(
    currentLanguage: AppLanguage,
    onSaveToProject: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var selectedTool by remember { mutableStateOf(StudioTool.ENHANCE) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var vignette by remember { mutableFloatStateOf(0.2f) }
    var beforeAfterSplit by remember { mutableFloatStateOf(0.5f) }
    var showBeforeAfter by remember { mutableStateOf(false) }

    var layers by remember {
        mutableStateOf(
            listOf(
                CanvasLayer("1", "بک‌گراند مرمر تیره (Background)", "IMAGE", "bg_obsidian"),
                CanvasLayer("2", "شیشه عطر با انعکاس طلا (Subject)", "IMAGE", "perfume_bottle"),
                CanvasLayer("3", "نورپردازی ساعت طلایی (Rim Light)", "EFFECT", "lighting_gold"),
                CanvasLayer("4", "تایپوگرافی لوگوی طرحی نو (Watermark)", "TEXT", "طرحی نو")
            )
        )
    }

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Brush, contentDescription = "Studio", tint = PrimaryGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.PERSIAN) "ناو استودیو (Nava Studio)" else "Nava Studio",
                                color = PrimaryGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "میز کار خلاقانه و ویرایش حرفه‌ای لایه‌ها" else "Creative editing suite & multi-layer canvas",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            brightness = 0f
                            contrast = 1f
                            saturation = 1f
                            vignette = 0.2f
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "تنظیمات بازنشانی شد" else "Reset to default"
                                )
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Interactive Studio Canvas
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF040607))
                        .border(1.dp, PrimaryGold.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Studio Canvas simulation with drawing
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Ambient studio backdrop
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(BrandGreen.copy(alpha = 0.4f + vignette), Color.Black),
                                center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                                radius = canvasWidth * 0.7f
                            )
                        )

                        // Central luxury subject art representation
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryGold.copy(alpha = 0.85f * contrast), BrandGreenGlow),
                                start = Offset(canvasWidth * 0.3f, canvasHeight * 0.3f),
                                end = Offset(canvasWidth * 0.7f, canvasHeight * 0.7f)
                            ),
                            radius = canvasHeight * 0.32f,
                            center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                        )

                        // Gold rim highlight curve
                        drawArc(
                            color = SoftGold.copy(alpha = 0.9f),
                            startAngle = 200f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(canvasWidth * 0.22f, canvasHeight * 0.22f),
                            size = Size(canvasHeight * 0.64f, canvasHeight * 0.64f),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Before / After divider line if toggled
                        if (showBeforeAfter) {
                            val splitX = canvasWidth * beforeAfterSplit
                            drawLine(
                                color = SoftGold,
                                start = Offset(splitX, 0f),
                                end = Offset(splitX, canvasHeight),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    // Floating Watermark badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "NAVA STUDIO • 8K", color = SoftGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Comparison Pill Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard.copy(alpha = 0.85f))
                            .clickable { showBeforeAfter = !showBeforeAfter }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Compare, contentDescription = "Compare", tint = SoftGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showBeforeAfter) "پایان مقایسه" else "مقایسه قبل/بعد",
                                color = SoftGold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (showBeforeAfter) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "اسلایدر مقایسه زنده قبل و بعد:", color = TextSecondary, fontSize = 11.sp)
                    Slider(
                        value = beforeAfterSplit,
                        onValueChange = { beforeAfterSplit = it },
                        colors = SliderDefaults.colors(thumbColor = PrimaryGold, activeTrackColor = BrandGreen)
                    )
                }
            }
        }

        // AI Tool Suite Chips (Specification #21)
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "ابزارهای هوش مصنوعی ناو استودیو:" else "Nava AI Tool Suite:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioTool.values().forEach { tool ->
                        val isSelected = selectedTool == tool
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BrandGreen else SurfaceCard)
                                .border(1.dp, if (isSelected) SoftGold else SurfaceCardBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedTool = tool }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.PERSIAN) tool.titleFa else tool.titleEn,
                                color = if (isSelected) SoftGold else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Adjustments Sliders
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "تنظیمات نور و رنگ (Fine Tuning):" else "Image Adjustments:",
                    color = PrimaryGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                AdjustmentSliderItem(
                    label = if (currentLanguage == AppLanguage.PERSIAN) "روشنایی (Brightness)" else "Brightness",
                    value = brightness,
                    range = -0.5f..0.5f,
                    onValueChange = { brightness = it }
                )

                AdjustmentSliderItem(
                    label = if (currentLanguage == AppLanguage.PERSIAN) "کنتراست (Contrast)" else "Contrast",
                    value = contrast,
                    range = 0.5f..1.8f,
                    onValueChange = { contrast = it }
                )

                AdjustmentSliderItem(
                    label = if (currentLanguage == AppLanguage.PERSIAN) "وینیت سینمایی (Vignette)" else "Vignette",
                    value = vignette,
                    range = 0f..0.8f,
                    onValueChange = { vignette = it }
                )
            }
        }

        // Layer Management (Specification #21)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
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
                        Icon(Icons.Default.Layers, contentDescription = "Layers", tint = SoftGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "لایه‌های فعال پروژه:" else "Project Layers:",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                layers.forEach { layer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgDark)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = layer.name, color = TextPrimary, fontSize = 11.sp)
                        Text(text = layer.type, color = SoftGold, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onSaveToProject("ویرایش ناو استودیو — ${selectedTool.name}")
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (currentLanguage == AppLanguage.PERSIAN) "در قالب پروژه ذخیره شد" else "Saved as Project"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = "Save Project", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "خروجی به پروژه استودیویی" else "Export to Studio Project",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AdjustmentSliderItem(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Text(text = String.format("%.2f", value), color = SoftGold, fontSize = 11.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = PrimaryGold, activeTrackColor = BrandGreen)
        )
    }
}
