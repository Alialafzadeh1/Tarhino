package com.example.data.remote.auth

import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.entity.UserEntity
import com.example.data.remote.api.TarhiNooApiService
import com.example.data.remote.config.BackendConfig
import com.example.data.remote.model.ApiResponse
import com.example.data.remote.model.AuthResponse
import com.example.data.remote.model.LoginRequest
import com.example.data.remote.model.RefreshTokenRequest
import com.example.data.remote.model.RegisterRequest
import com.example.data.remote.model.UserDto
import com.example.data.remote.realtime.RealtimeManager
import com.example.data.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real authentication repository connecting to the backend auth endpoints.
 * In Release builds, fake local auth fallback is strictly disabled.
 * In Debug builds, local demo mode is available for offline UI testing.
 */
class AuthRepository(
    private val apiService: TarhiNooApiService?,
    private val sessionManager: SessionManager,
    private val db: AppDatabase
) {

    suspend fun login(email: String, pass: String): Result<UserDto> = withContext(Dispatchers.IO) {
        if (!BackendConfig.isBackendConfigured || apiService == null) {
            if (BuildConfig.DEBUG) {
                // Local demo mode only permitted in DEBUG
                val localUser = UserDto(
                    id = "1",
                    username = email.substringBefore("@").lowercase(),
                    displayName = email.substringBefore("@"),
                    bio = "کاربر محلی (دموی محیط توسعه)",
                    isOnline = true
                )
                sessionManager.saveSession(localUser, "debug_dev_token_${System.currentTimeMillis()}", "debug_refresh", 86400)
                return@withContext Result.success(localUser)
            } else {
                return@withContext Result.failure(Exception("بک‌اند متصل نیست. لطفاً آدرس سرور را در پیکربندی بررسی کنید."))
            }
        }

        try {
            val response = apiService.login(LoginRequest(email, pass))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                sessionManager.saveSession(data.user, data.accessToken, data.refreshToken, data.expiresIn)
                // Cache user in Room
                val userEntity = UserEntity(
                    id = data.user.id.toLongOrNull() ?: 0L,
                    username = data.user.username,
                    displayName = data.user.displayName,
                    avatarUrl = data.user.avatarUrl,
                    bio = data.user.bio,
                    isOnline = true
                )
                db.messengerUserDao().insertUser(userEntity)
                Result.success(data.user)
            } else {
                val err = response.body()?.error?.message ?: "اطلاعات ورود نامعتبر است"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        pass: String,
        username: String,
        displayName: String
    ): Result<UserDto> = withContext(Dispatchers.IO) {
        if (!BackendConfig.isBackendConfigured || apiService == null) {
            if (BuildConfig.DEBUG) {
                val cleanUsername = username.removePrefix("@").trim().lowercase()
                val localUser = UserDto(
                    id = "1",
                    username = cleanUsername,
                    displayName = displayName.ifBlank { cleanUsername },
                    bio = "کاربر محلی جدید (دموی محیط توسعه)",
                    isOnline = true
                )
                sessionManager.saveSession(localUser, "debug_dev_token_${System.currentTimeMillis()}", "debug_refresh", 86400)
                return@withContext Result.success(localUser)
            } else {
                return@withContext Result.failure(Exception("بک‌اند متصل نیست. ثبت‌نام نیازمند اتصال به سرور واقعی است."))
            }
        }

        try {
            val response = apiService.register(
                RegisterRequest(email, pass, username.removePrefix("@").trim().lowercase(), displayName)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                sessionManager.saveSession(data.user, data.accessToken, data.refreshToken, data.expiresIn)
                Result.success(data.user)
            } else {
                val err = response.body()?.error?.message ?: "خطا در ثبت‌نام کاربر"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Startup Session Restore sequence as mandated:
     * 1. Check SessionManager for existing tokens
     * 2. Inspect access token
     * 3. Check expiration
     * 4. Refresh token via backend if expired
     * 5. Fetch /users/me
     * 6. Cache user in Room
     * 7. Connect Realtime
     * 8. Initial Sync
     */
    suspend fun restoreSessionAndSync(
        realtimeManager: RealtimeManager,
        syncManager: SyncManager
    ) = withContext(Dispatchers.IO) {
        val currentToken = sessionManager.getAccessToken() ?: return@withContext
        val currentUserId = sessionManager.getCurrentUserId() ?: return@withContext

        if (!BackendConfig.isBackendConfigured || apiService == null) {
            return@withContext
        }

        var validToken = currentToken

        // Step 3 & 4: Check expiration and refresh if needed
        if (sessionManager.isTokenExpired()) {
            val refreshToken = sessionManager.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                try {
                    val refreshResponse = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                    if (refreshResponse.isSuccessful && refreshResponse.body()?.success == true) {
                        val authData = refreshResponse.body()!!.data!!
                        sessionManager.saveSession(
                            authData.user,
                            authData.accessToken,
                            authData.refreshToken,
                            authData.expiresIn
                        )
                        validToken = authData.accessToken
                    } else {
                        sessionManager.markSessionExpired("نشست کاربری منقضی شد")
                        realtimeManager.disconnect()
                        return@withContext
                    }
                } catch (e: Exception) {
                    // Offline or server unreachable, preserve local session
                }
            }
        }

        // Step 5 & 6: Fetch /users/me and cache in Room
        try {
            val meResponse = apiService.getCurrentUser()
            if (meResponse.isSuccessful && meResponse.body()?.success == true) {
                val me = meResponse.body()!!.data!!
                val userEntity = UserEntity(
                    id = me.id.toLongOrNull() ?: 0L,
                    username = me.username,
                    displayName = me.displayName,
                    avatarUrl = me.avatarUrl,
                    bio = me.bio,
                    isOnline = true
                )
                db.messengerUserDao().insertUser(userEntity)
            }
        } catch (_: Exception) {}

        // Step 7 & 8: Connect Realtime and Initial Sync
        realtimeManager.connect(validToken, currentUserId)
        syncManager.performInitialSync()
        syncManager.retryPendingMessages()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            if (BackendConfig.isBackendConfigured && apiService != null) {
                apiService.logout()
            }
        } catch (_: Exception) {
        } finally {
            sessionManager.clearSession()
        }
    }
}
