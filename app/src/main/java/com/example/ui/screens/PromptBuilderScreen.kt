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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.PromptBuilderState
import com.example.ai.PromptEngine
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
fun PromptBuilderScreen(
    currentLanguage: AppLanguage,
    initialSubject: String = "",
    initialStyle: String = "Cinematic",
    initialLighting: String = "Cinematic",
    onSaveToLibrary: (title: String, prompt: String, negative: String, isVideo: Boolean) -> Unit,
    onCreateProjectWithPrompt: (title: String, prompt: String) -> Unit,
    onNavigateToOptimizer: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var isVideoMode by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf(initialSubject) }
    var selectedStyle by remember { mutableStateOf(initialStyle) }
    var selectedComposition by remember { mutableStateOf("Medium Shot") }
    var selectedLighting by remember { mutableStateOf(initialLighting) }
    var selectedLens by remember { mutableStateOf("50mm") }
    var selectedEnvironment by remember { mutableStateOf("Studio") }
    var selectedMaterials by remember { mutableStateOf(listOf("Glass", "Gold")) }
    var colors by remember { mutableStateOf("Gold, Deep Emerald") }
    var mood by remember { mutableStateOf("Sophisticated, Luxury") }
    var quality by remember { mutableStateOf("8K, Photorealistic, Unreal Engine 5") }
    var customNegative by remember { mutableStateOf("") }

    // Video specific
    var cameraMovement by remember { mutableStateOf("Orbit") }
    var durationSeconds by remember { mutableFloatStateOf(8f) }
    var temporalConsistency by remember { mutableStateOf(true) }

    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val generatedPromptPair by remember {
        derivedStateOf {
            val state = PromptBuilderState(
                isVideo = isVideoMode,
                subject = subject,
                style = selectedStyle,
                composition = selectedComposition,
                lens = selectedLens,
                lighting = selectedLighting,
                environment = selectedEnvironment,
                materials = selectedMaterials,
                colors = colors,
                mood = mood,
                quality = quality,
                customNegativePrompt = customNegative,
                cameraMovement = cameraMovement,
                durationSeconds = durationSeconds.toInt(),
                temporalConsistency = temporalConsistency
            )
            PromptEngine.buildPrompt(state)
        }
    }

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
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "معمار پرامپت (Prompt Architect)" else "Prompt Architect",
                            color = PrimaryGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "طراحی هندسی، لایه‌ای و هوشمندانه پرامپت" else "Structured multi-parameter prompt generation",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Mode Switch: Image vs Video
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard)
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isVideoMode) BrandGreen else Color.Transparent)
                                .clickable { isVideoMode = false }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Photo, contentDescription = "Image", tint = if (!isVideoMode) SoftGold else TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (currentLanguage == AppLanguage.PERSIAN) "تصویر" else "Image", color = if (!isVideoMode) SoftGold else TextMuted, fontSize = 11.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isVideoMode) BrandGreen else Color.Transparent)
                                .clickable { isVideoMode = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Movie, contentDescription = "Video", tint = if (isVideoMode) SoftGold else TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (currentLanguage == AppLanguage.PERSIAN) "ویدیو" else "Video", color = if (isVideoMode) SoftGold else TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Generated Output Box (Sticky Preview)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, PrimaryGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Live", tint = PrimaryGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "پیش‌نمایش زنده پرامپت منسجم:" else "Live Coherent Prompt Preview:",
                            color = PrimaryGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(generatedPromptPair.first))
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (currentLanguage == AppLanguage.PERSIAN) "پرامپت کپی شد" else "Prompt copied"
                                    )
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SoftGold, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = generatedPromptPair.first,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                if (generatedPromptPair.second.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Negative: ${generatedPromptPair.second}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val title = if (subject.isNotBlank()) subject.take(30) else "پرامپت معمار طرحی نو"
                            onSaveToLibrary(title, generatedPromptPair.first, generatedPromptPair.second, isVideoMode)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "در کتابخانه ذخیره شد" else "Saved to Library"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = SoftGold),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Save", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "ذخیره" else "Save", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            onNavigateToOptimizer(generatedPromptPair.first)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Optimize", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "بهینه‌ساز AI" else "Optimize AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val title = if (subject.isNotBlank()) subject.take(30) else "پروژه طرحی نو"
                            onCreateProjectWithPrompt(title, generatedPromptPair.first)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "پروژه با موفقیت ایجاد شد" else "Project created"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardBorder, contentColor = TextPrimary),
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = "Project", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "ساخت پروژه" else "Project", fontSize = 11.sp)
                    }
                }
            }
        }

        // Section: Subject Input
        item {
            BuilderSection(
                title = if (currentLanguage == AppLanguage.PERSIAN) "۱. سوژه اصلی (Subject)" else "1. Subject & Core Concept",
                description = if (currentLanguage == AppLanguage.PERSIAN) "توصیف مختصر چیزی که می‌خواهید خلق شود" else "Describe what you want to create"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    if (subject.isEmpty()) {
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN)
                                "مثال: بطری شیشه‌ای عطر لوکس با مایع کهربایی، یا پوستر عاشورا..."
                            else
                                "e.g., Luxury perfume bottle with amber liquid...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                    BasicTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section: Style
        item {
            BuilderSection(
                title = if (currentLanguage == AppLanguage.PERSIAN) "۲. سبک بصری (Style)" else "2. Visual Style",
                description = if (currentLanguage == AppLanguage.PERSIAN) "انتخاب لحن هنری و بصری اثر" else "Select the artistic rendering style"
            ) {
                ChipSelectorRow(
                    items = PromptEngine.availableStyles,
                    selectedItem = selectedStyle,
                    onSelect = { selectedStyle = it }
                )
            }
        }

        // Section: Composition & Camera
        item {
            BuilderSection(
                title = if (currentLanguage == AppLanguage.PERSIAN) "۳. ترکیب‌بندی و کادربندی (Composition)" else "3. Composition & Framing",
                description = if (currentLanguage == AppLanguage.PERSIAN) "فاصله و زاویه قرارگیری دوربین" else "Camera shot type and framing"
            ) {
                ChipSelectorRow(
                    items = PromptEngine.availableComposition,
                    selectedItem = selectedComposition,
                    onSelect = { selectedComposition = it }
                )
            }
        }

        // Section: Lens
        item {
            BuilderSection(
                title = if (currentLanguage == AppLanguage.PERSIAN) "۴. فاصله کانونی لنز (Lens)" else "4. Camera Lens",
                description = if (currentLanguage == AppLanguage.PERSIAN) "عمق میدان و پرسپکتیو اپتیکال" else "Focal length and optical perspective"
            ) {
                ChipSelectorRow(
                    items = PromptEngine.availableLenses,
                    selectedItem = selectedLens,
                    onSelect = { selectedLens = it }
                )
            }
        }

        // Section: Lighting
        item {
            BuilderSection(
                title = if (currentLanguage == AppLanguage.PERSIAN) "۵. نورپردازی (Lighting)" else "5. Lighting Atmosphere",
                description = if (currentLanguage == AppLanguage.PERSIAN) "جهت و جنس نور" else "Lighting mood and setup"
            ) {
                ChipSelectorRow(
                    items = PromptEngine.availableLighting,
                    selectedItem = selectedLighting,
                    onSelect = { selectedLighting = it }
                )
            }
        }

        // Section: Environment
        item {
            BuilderSection(
                title = if (currentLanguage == AppLanguage.PERSIAN) "۶. محیط و پس‌زمینه (Environment)" else "6. Environment",
                description = if (currentLanguage == AppLanguage.PERSIAN) "لوکیشن و فضای قرارگیری سوژه" else "Setting and atmosphere"
            ) {
                ChipSelectorRow(
                    items = PromptEngine.availableEnvironments,
                    selectedItem = selectedEnvironment,
                    onSelect = { selectedEnvironment = it }
                )
            }
        }

        // Section: Materials (Multi-select)
        item {
            BuilderSection(
                title = if (currentLanguage == AppLanguage.PERSIAN) "۷. متریال‌ها و بافت (Materials)" else "7. Materials & Textures",
                description = if (currentLanguage == AppLanguage.PERSIAN) "جنس سطوح و انعکاس‌ها" else "Surface materials and tactile feel"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PromptEngine.availableMaterials.forEach { mat ->
                        val isSelected = selectedMaterials.contains(mat)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BrandGreen else SurfaceCard)
                                .border(1.dp, if (isSelected) SoftGold else SurfaceCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedMaterials = if (isSelected) {
                                        selectedMaterials - mat
                                    } else {
                                        selectedMaterials + mat
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mat,
                                color = if (isSelected) SoftGold else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Video-specific settings when Video is selected
        if (isVideoMode) {
            item {
                BuilderSection(
                    title = if (currentLanguage == AppLanguage.PERSIAN) "۸. حرکت دوربین ویدیو (Camera Motion)" else "8. Video Camera Movement",
                    description = if (currentLanguage == AppLanguage.PERSIAN) "مسیر حرکت در فضا برای تیزر و ویدیو" else "Dynamic camera path"
                ) {
                    ChipSelectorRow(
                        items = PromptEngine.availableCameraMovements,
                        selectedItem = cameraMovement,
                        onSelect = { cameraMovement = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN)
                            "مدت زمان ویدیو: ${durationSeconds.toInt()} ثانیه"
                        else
                            "Duration: ${durationSeconds.toInt()} seconds",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = durationSeconds,
                        onValueChange = { durationSeconds = it },
                        valueRange = 4f..16f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryGold,
                            activeTrackColor = BrandGreen
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "پایداری زمانی فریم‌ها (Temporal Consistency)" else "Temporal Consistency",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Switch(
                            checked = temporalConsistency,
                            onCheckedChange = { temporalConsistency = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryGold,
                                checkedTrackColor = BrandGreen
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BuilderSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = description, color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun ChipSelectorRow(
    items: List<String>,
    selectedItem: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isSelected = selectedItem == item
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) BrandGreen else SurfaceCard)
                    .border(1.dp, if (isSelected) SoftGold else SurfaceCardBorder, RoundedCornerShape(8.dp))
                    .clickable { onSelect(item) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item,
                    color = if (isSelected) SoftGold else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
