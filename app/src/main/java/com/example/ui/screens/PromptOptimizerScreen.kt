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
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.PromptEngine
import com.example.domain.model.AppLanguage
import com.example.domain.model.ModelTarget
import com.example.domain.model.OutputMode
import com.example.ui.components.TarhiNooAICore
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
fun PromptOptimizerScreen(
    currentLanguage: AppLanguage,
    initialPrompt: String = "",
    onSaveOptimizedPrompt: (title: String, prompt: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var rawInput by remember { mutableStateOf(initialPrompt) }
    var selectedMode by remember { mutableStateOf(OutputMode.CINEMATIC) }
    var selectedModel by remember { mutableStateOf(ModelTarget.IMAGEN) }
    var optimizedResult by remember { mutableStateOf("") }
    var isOptimizing by remember { mutableStateOf(false) }

    val clipboard = LocalClipboardManager.current
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
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "بهینه‌ساز پرامپت (AI Optimizer)" else "AI Prompt Optimizer",
                    color = PrimaryGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN)
                        "تبدیل پرامپت‌های خام یا ضعیف به فرمول‌های خروجی درجه یک و استاندارد استودیو"
                    else
                        "Transform raw ideas into publication-grade prompts for cutting-edge AI models",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Raw Prompt Input Box
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
                    text = if (currentLanguage == AppLanguage.PERSIAN) "ایده اولیه یا پرامپت خام:" else "Raw Prompt / Idea:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgDark)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    if (rawInput.isEmpty()) {
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN)
                                "پرامپت خود را اینجا بنویسید یا جای‌گذاری کنید (مثلاً: عکس پرتره پیرمرد کویر در غروب)..."
                            else
                                "Type or paste your base prompt here...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                    BasicTextField(
                        value = rawInput,
                        onValueChange = { rawInput = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Optimization Mode Chips (7 modes from specification #16)
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "حالت بازنویسی و لحن استودیویی:" else "Optimization Preset:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutputMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandGreen else SurfaceCard)
                                .border(1.dp, if (isSelected) SoftGold else SurfaceCardBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedMode = mode }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (currentLanguage == AppLanguage.PERSIAN) mode.titleFa else mode.titleEn,
                                    color = if (isSelected) SoftGold else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Target Model Selector
        if (selectedMode == OutputMode.MODEL_OPTIMIZED) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "موتور هوش مصنوعی هدف:" else "Target AI Engine:",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModelTarget.values().forEach { model ->
                            val isSelected = selectedModel == model
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandGreen else SurfaceCard)
                                    .border(1.dp, if (isSelected) PrimaryGold else SurfaceCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedModel = model }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = model.displayName,
                                    color = if (isSelected) SoftGold else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Optimize Button
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
                Button(
                    onClick = {
                        val input = if (rawInput.isBlank()) "Luxury perfume bottle, dark obsidian" else rawInput
                        optimizedResult = PromptEngine.optimizePrompt(input, selectedMode, selectedModel)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (currentLanguage == AppLanguage.PERSIAN) "پرامپت با موفقیت بهینه‌سازی شد" else "Prompt optimized"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Optimize", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "بهینه‌سازی پرامپت با هوش مصنوعی" else "Optimize Prompt Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Optimized Result Card
        if (optimizedResult.isNotBlank()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SoftGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TarhiNooAICore(size = 28.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.PERSIAN) "پرامپت بهینه‌شده طرحی نو:" else "Tarhi Noo Optimized Prompt:",
                                color = PrimaryGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(optimizedResult))
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (currentLanguage == AppLanguage.PERSIAN) "کپی شد" else "Copied"
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SoftGold, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgDark)
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = optimizedResult,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val title = if (rawInput.isNotBlank()) rawInput.take(25) else "پرامپت بهینه‌شده"
                            onSaveOptimizedPrompt(title, optimizedResult)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "به پرامپت‌های من اضافه شد" else "Saved to My Prompts"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = SoftGold),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "ذخیره در پرامپت‌های من" else "Save to My Prompts")
                    }
                }
            }
        }
    }
}
