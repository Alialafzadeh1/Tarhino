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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ProjectEntity
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

@Composable
fun ProjectsScreen(
    currentLanguage: AppLanguage,
    projects: List<ProjectEntity>,
    onCreateProject: (title: String, description: String, category: String) -> Unit,
    onDeleteProject: (Long) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                            text = if (currentLanguage == AppLanguage.PERSIAN) "مدیریت پروژه‌ها (Projects)" else "Projects Workspace",
                            color = PrimaryGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLanguage == AppLanguage.PERSIAN) "فضای یکپارچه مدیریت دارایی‌ها، پرامپت‌ها و کمپین‌ها" else "Workspace for campaigns, prompts, and assets",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "پروژه جدید" else "New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (projects.isEmpty()) {
                item {
                    EmptyState(
                        title = if (currentLanguage == AppLanguage.PERSIAN) "هنوز پروژه‌ای ساخته نشده است" else "No projects created yet",
                        description = if (currentLanguage == AppLanguage.PERSIAN)
                            "ایده‌ها، پرامپت‌ها و خروجی‌های استودیویی خود را در پروژه‌های مجزا سازماندهی کنید."
                        else
                            "Organize your prompts, creative briefs, and visual outputs.",
                        buttonText = if (currentLanguage == AppLanguage.PERSIAN) "+ ساخت اولین پروژه" else "+ Create First Project",
                        onButtonClick = { showCreateDialog = true }
                    )
                }
            } else {
                items(projects) { project ->
                    ProjectDetailCard(
                        project = project,
                        currentLanguage = currentLanguage,
                        onDelete = {
                            onDeleteProject(project.id)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.PERSIAN) "پروژه حذف شد" else "Project deleted"
                                )
                            }
                        }
                    )
                }
            }
        }

        if (showCreateDialog) {
            CreateProjectDialog(
                currentLanguage = currentLanguage,
                onDismiss = { showCreateDialog = false },
                onCreate = { title, desc, cat ->
                    onCreateProject(title, desc, cat)
                    showCreateDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (currentLanguage == AppLanguage.PERSIAN) "پروژه جدید با موفقیت ساخته شد" else "Project created successfully"
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ProjectDetailCard(
    project: ProjectEntity,
    currentLanguage: AppLanguage,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrandGreen.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = "Project", tint = SoftGold, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = project.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = project.category, color = SoftGold, fontSize = 11.sp)
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }

            if (project.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = project.description, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${project.promptCount} پرامپت • ${project.assetCount} دارایی",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandGreen.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = project.status, color = SoftGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("تبلیغاتی و محصول") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "ساخت پروژه جدید" else "New Project",
                    color = PrimaryGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = if (currentLanguage == AppLanguage.PERSIAN) "عنوان پروژه:" else "Project Title:", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgDark)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = if (currentLanguage == AppLanguage.PERSIAN) "توضیحات مختصر:" else "Description:", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgDark)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    BasicTextField(
                        value = description,
                        onValueChange = { description = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp),
                        cursorBrush = SolidColor(PrimaryGold),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextMuted)
                    ) {
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "انصراف" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title, description, category)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (currentLanguage == AppLanguage.PERSIAN) "ایجاد پروژه" else "Create")
                    }
                }
            }
        }
    }
}
