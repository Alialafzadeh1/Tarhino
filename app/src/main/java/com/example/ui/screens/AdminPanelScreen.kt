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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PromptEntity
import com.example.domain.model.AppLanguage
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
fun AdminPanelScreen(
    currentLanguage: AppLanguage,
    promptCount: Int,
    projectCount: Int,
    onAddPrompt: (PromptEntity) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var newTitle by remember { mutableStateOf("") }
    var newPrompt by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("commercial") }

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = "Admin", tint = PrimaryGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.PERSIAN) "پنل مدیریت سامانه (Admin Dashboard)" else "Admin Dashboard",
                        color = PrimaryGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "نظارت بر کارایی سیستم، پرامپت‌ها و گزارش‌های مصرف هوش مصنوعی" else "System metrics, prompt repository & AI operations",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = if (currentLanguage == AppLanguage.PERSIAN) "کل پرامپت‌ها" else "Prompts",
                    value = "$promptCount",
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = if (currentLanguage == AppLanguage.PERSIAN) "پروژه‌ها" else "Projects",
                    value = "$projectCount",
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = if (currentLanguage == AppLanguage.PERSIAN) "وضعیت موتور" else "Status",
                    value = "Online",
                    isSuccess = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Inject Prompt to DB
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "افزودن پرامپت مستقیم به دیتابیس سامانه:" else "Publish Prompt to Database:",
                    color = PrimaryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "عنوان پرامپت:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgDark)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    BasicTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "متن کامل پرامپت:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgDark)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    BasicTextField(
                        value = newPrompt,
                        onValueChange = { newPrompt = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newPrompt.isNotBlank()) {
                            onAddPrompt(
                                PromptEntity(
                                    type = "IMAGE",
                                    title = newTitle,
                                    description = "پرامپت ثبت‌شده از پنل مدیریت",
                                    prompt = newPrompt,
                                    categoryId = newCategory,
                                    author = "طرحینه مدیا / مدیریت"
                                )
                            )
                            newTitle = ""
                            newPrompt = ""
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "پرامپت به دیتابیس افزوده شد" else "Prompt published to DB"
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (currentLanguage == AppLanguage.PERSIAN) "ثبت در کتابخانه رسمی" else "Save to Official Library", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    isSuccess: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, color = TextMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = if (isSuccess) SuccessGreen else PrimaryGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
