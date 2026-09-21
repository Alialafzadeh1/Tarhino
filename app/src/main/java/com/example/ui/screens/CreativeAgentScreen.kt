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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RocketLaunch
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
import com.example.ai.CreativeAgentEngine
import com.example.domain.model.AppLanguage
import com.example.domain.model.CreativeAgentWorkflowResult
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
fun CreativeAgentScreen(
    currentLanguage: AppLanguage,
    onCreateProject: (title: String, description: String, promptText: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var briefInput by remember { mutableStateOf("کمپین اختصاصی عطر لوکس ماهور با سنگ آبسیدین و انعکاس کهربا") }
    var workflowResult by remember { mutableStateOf<CreativeAgentWorkflowResult?>(null) }
    var isExecuting by remember { mutableStateOf(false) }

    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val steps = listOf(
        "تشخیص قصد (Intent Detection)",
        "تحلیل محصول و برند (Product Analysis)",
        "جهت‌گیری خلاقانه (Creative Direction)",
        "مهندسی پرامپت تصویر استودیویی",
        "مهندسی پرامپت ویدیوی سینمایی",
        "نگارش کپشن و هشتگ‌های تخصصی",
        "یکپارچه‌سازی و آماده‌سازی ساخت پروژه"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TarhiNooAICore(size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "دستیار خلاق طرحی نو (Creative Agent)" else "Tarhi Noo Creative Agent",
                            color = PrimaryGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "اجرای خودکار چرخه کامل کمپین و کارگردانی هنری" else "Autonomous end-to-end creative direction pipeline",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Brief Input Box
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
                    text = if (currentLanguage == AppLanguage.PERSIAN) "شرح بریف یا ایده کلی کمپین:" else "Campaign Brief / Idea:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgDark)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = briefInput,
                        onValueChange = { briefInput = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        workflowResult = CreativeAgentEngine.executeWorkflow(briefInput)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (currentLanguage == AppLanguage.PERSIAN) "چرخه هوش مصنوعی کامل شد" else "Workflow completed"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = "Run", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "اجرای خودکار چرخه خلاق (Execute Workflow)" else "Execute Creative Workflow",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Visual Pipeline Progress Tracker
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
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "مراحل چرخه هوش مصنوعی:" else "Autonomous Workflow Steps:",
                    color = PrimaryGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Done",
                            tint = if (workflowResult != null) SuccessGreen else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step,
                            color = if (workflowResult != null) TextPrimary else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Result Showcase
        workflowResult?.let { result ->
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SoftGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = result.title,
                            color = PrimaryGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                val fullText = "${result.title}\n\nتحلیل محصول:\n${result.productAnalysis}\n\nجهت‌گیری هنری:\n${result.creativeDirection}\n\nپرامپت تصویر:\n${result.imagePrompt}\n\nپرامپت ویدیو:\n${result.videoPrompt}\n\nکپشن:\n${result.caption}"
                                clipboard.setText(AnnotatedString(fullText))
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (currentLanguage == AppLanguage.PERSIAN) "پکیج کامل کپی شد" else "Full packet copied"
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy All", tint = SoftGold, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ResultSection("تحلیل محصول", result.productAnalysis)
                    ResultSection("جهت‌گیری هنری", result.creativeDirection)
                    ResultSection("پرامپت تصویر استودیویی", result.imagePrompt)
                    ResultSection("پرامپت ویدیوی سینمایی (Veo)", result.videoPrompt)
                    ResultSection("کپشن آماده انتشار", result.caption)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "هشتگ‌های پیشنهادی: ${result.hashtags.joinToString(" ")}", color = SoftGold, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onCreateProject(result.title, result.productAnalysis, result.imagePrompt)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "پروژه رسمی با موفقیت ایجاد شد" else "Official Project created"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = SoftGold),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = "Create Project", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "ایجاد پروژه از این کمپین" else "Create Project from Workflow", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BgDark)
                .border(0.8.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Text(text = content, color = TextPrimary, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}
