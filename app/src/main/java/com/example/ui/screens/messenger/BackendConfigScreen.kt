package com.example.ui.screens.messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.remote.auth.AuthRepository
import com.example.data.remote.auth.AuthState
import com.example.data.remote.auth.SessionManager
import com.example.data.remote.config.BackendConfig
import com.example.data.remote.config.BackendMode
import com.example.data.remote.realtime.RealtimeConnectionState
import com.example.data.remote.realtime.RealtimeManager
import com.example.data.sync.SyncManager
import com.example.domain.model.AppLanguage
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BgDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Backend status & developer configuration panel for Tarhi Noo Messenger (Phase 9).
 * Displays connection state, authenticated user, realtime health, and allows safe session management.
 * Shows "BACKEND NOT CONFIGURED" banner when .env does not specify BACKEND_BASE_URL.
 */
@Composable
fun BackendConfigScreen(
    currentLanguage: AppLanguage,
    sessionManager: SessionManager,
    realtimeManager: RealtimeManager,
    syncManager: SyncManager,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    val scope = rememberCoroutineScope()

    val authState by sessionManager.authState.collectAsState()
    val realtimeState by realtimeManager.connectionState.collectAsState()
    val syncStatusText by syncManager.syncState.collectAsState()
    val lastSyncTime by syncManager.lastSyncTime.collectAsState()

    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryGold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isFa) "پیکربندی سرور و همگام‌سازی" else "Backend & Sync Config",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isFa) "وضعیت ارتباط چندکاربره و بی‌درنگ (فاز ۹)" else "Multi-User & Realtime Engine Status (Phase 9)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Connection Mode Banner
            item {
                val isConfigured = BackendConfig.isBackendConfigured
                val bannerBg = if (isConfigured) BrandGreen else SurfaceDark
                val bannerBorder = if (isConfigured) PrimaryGold else WarningOrange

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(bannerBg)
                        .border(1.dp, bannerBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isConfigured) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = if (isConfigured) PrimaryGold else WarningOrange,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (isConfigured) {
                                    if (isFa) "متصل به سرور و درگاه بی‌درنگ" else "BACKEND CONNECTED"
                                } else {
                                    if (isFa) "پیکربندی سرور تنظیم نشده است (حالت محلی فعال)" else "BACKEND NOT CONFIGURED (LOCAL MODE)"
                                },
                                color = if (isConfigured) PrimaryGold else WarningOrange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isConfigured) {
                                    if (isFa) "ارتباط کامل با درگاه REST و وب‌سوکت برقرار است." else "REST API and WebSocket gateway active."
                                } else {
                                    if (isFa) "تنظیمات آدرس در .env تکمیل نشده است. اپلیکیشن با پایگاه‌داده امن Room و پیام‌رسان محلی فعال است." else "BACKEND_BASE_URL is not configured in .env. Falling back to local Room database."
                                },
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // Status Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isFa) "وضعیت موتور بی‌درنگ (Realtime Manager)" else "Realtime Engine Status",
                            color = PrimaryGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Realtime State
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isFa) "اتصال وب‌سوکت:" else "WebSocket Status:", color = TextSecondary, fontSize = 12.sp)
                            val stateColor = when (realtimeState) {
                                RealtimeConnectionState.CONNECTED -> SuccessGreen
                                RealtimeConnectionState.CONNECTING -> AccentCyan
                                RealtimeConnectionState.RECONNECTING -> WarningOrange
                                RealtimeConnectionState.FAILED -> ErrorRed
                                RealtimeConnectionState.DISCONNECTED -> TextMuted
                            }
                            Text(
                                text = realtimeState.name,
                                color = stateColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Sync State
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isFa) "وضعیت همگام‌ساز (Sync):" else "Sync Engine:", color = TextSecondary, fontSize = 12.sp)
                            Text(text = syncStatusText, color = TextPrimary, fontSize = 12.sp)
                        }

                        // Last Sync
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isFa) "آخرین بررسی سرور:" else "Last Checked:", color = TextSecondary, fontSize = 12.sp)
                            val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastSyncTime))
                            Text(text = formattedTime, color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Auth State & Identity Management
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isFa) "هویت کاربر احراز‌شده (Session Management)" else "Authenticated Session",
                            color = PrimaryGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        when (val state = authState) {
                            is AuthState.Authenticated -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(BrandGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Person, null, tint = PrimaryGold, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = state.user.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "@${state.user.username}", color = PrimaryGold, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            authRepository.logout()
                                            snackbarHostState.showSnackbar(if (isFa) "از حساب کاربری خارج شدید" else "Logged out")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                    modifier = Modifier.fillMaxWidth().border(0.8.dp, ErrorRed, RoundedCornerShape(8.dp))
                                ) {
                                    Text(text = if (isFa) "خروج از حساب کاربری (Logout)" else "Logout", color = ErrorRed, fontSize = 12.sp)
                                }
                            }
                            is AuthState.NoSession, is AuthState.SessionExpired, is AuthState.AuthError -> {
                                Text(
                                    text = if (isFa) "در حال حاضر هیچ حسابی لاگین نیست. می‌توانید با یک شناسه دلخواه ثبت‌نام یا ورود کنید:" else "No active authenticated session. Login or test with custom credentials:",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )

                                // Email input
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BgDark, RoundedCornerShape(8.dp))
                                        .border(0.8.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    BasicTextField(
                                        value = loginEmail,
                                        onValueChange = { loginEmail = it },
                                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                                        cursorBrush = SolidColor(PrimaryGold),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { inner ->
                                            if (loginEmail.isEmpty()) {
                                                Text(if (isFa) "ایمیل یا نام کاربری (مثال: ali@tarhineh.ir)" else "Email or Username", color = TextMuted, fontSize = 12.sp)
                                            }
                                            inner()
                                        }
                                    )
                                }

                                // Password input
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BgDark, RoundedCornerShape(8.dp))
                                        .border(0.8.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    BasicTextField(
                                        value = loginPassword,
                                        onValueChange = { loginPassword = it },
                                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                                        cursorBrush = SolidColor(PrimaryGold),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { inner ->
                                            if (loginPassword.isEmpty()) {
                                                Text(if (isFa) "کلمه عبور" else "Password", color = TextMuted, fontSize = 12.sp)
                                            }
                                            inner()
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (loginEmail.isBlank()) return@Button
                                            isSubmitting = true
                                            scope.launch {
                                                val res = authRepository.login(loginEmail, loginPassword.ifBlank { "pass123" })
                                                isSubmitting = false
                                                if (res.isSuccess) {
                                                    snackbarHostState.showSnackbar(if (isFa) "با موفقیت وارد شدید" else "Logged in successfully")
                                                } else {
                                                    snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "خطا")
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                        modifier = Modifier.weight(1f).border(1.dp, PrimaryGold, RoundedCornerShape(8.dp))
                                    ) {
                                        Text(text = if (isFa) "ورود (Login)" else "Login", color = PrimaryGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            if (loginEmail.isBlank()) return@Button
                                            isSubmitting = true
                                            scope.launch {
                                                val cleanName = loginEmail.substringBefore("@")
                                                val res = authRepository.register(loginEmail, loginPassword.ifBlank { "pass123" }, cleanName, cleanName)
                                                isSubmitting = false
                                                if (res.isSuccess) {
                                                    snackbarHostState.showSnackbar(if (isFa) "ثبت‌نام انجام شد" else "Registered successfully")
                                                } else {
                                                    snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "خطا")
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                        modifier = Modifier.weight(1f).border(0.8.dp, SoftGold, RoundedCornerShape(8.dp))
                                    ) {
                                        Text(text = if (isFa) "ثبت‌نام جدید" else "Register", color = SoftGold, fontSize = 12.sp)
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Sync Actions
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isFa) "عملیات همگام‌سازی دستی" else "Manual Sync Control",
                            color = PrimaryGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    syncManager.performInitialSync()
                                    snackbarHostState.showSnackbar(if (isFa) "درخواست همگام‌سازی اجرا شد" else "Sync completed")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth().border(0.8.dp, PrimaryGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Filled.Sync, null, tint = PrimaryGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (isFa) "اجرای همگام‌سازی سریع با سرور" else "Trigger Manual Sync", color = PrimaryGold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
