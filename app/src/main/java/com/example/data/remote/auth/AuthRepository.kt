package com.example.data.remote.auth

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real authentication repository connecting to the backend auth endpoints
 * with fallback to local session when backend is not yet configured.
 */
class AuthRepository(
    private val apiService: TarhiNooApiService?,
    private val sessionManager: SessionManager,
    private val db: AppDatabase
) {

    suspend fun login(email: String, pass: String): Result<UserDto> = withContext(Dispatchers.IO) {
        if (!BackendConfig.isBackendConfigured || apiService == null) {
            // Local-only simulated auth login for demonstration when backend credentials not yet injected
            val localUser = UserDto(
                id = "1",
                username = email.substringBefore("@").lowercase(),
                displayName = email.substringBefore("@"),
                bio = "کاربر طرحی نو",
                isOnline = true
            )
            sessionManager.saveSession(localUser, "local_dev_token_${System.currentTimeMillis()}", "local_refresh", 86400)
            return@withContext Result.success(localUser)
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
                val err = response.body()?.error?.message ?: "خطا در ورود به حساب کاربری"
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
            val cleanUsername = username.removePrefix("@").trim().lowercase()
            val localUser = UserDto(
                id = "1",
                username = cleanUsername,
                displayName = displayName.ifBlank { cleanUsername },
                bio = "کاربر جدید طرحی نو",
                isOnline = true
            )
            sessionManager.saveSession(localUser, "local_dev_token_${System.currentTimeMillis()}", "local_refresh", 86400)
            return@withContext Result.success(localUser)
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
                val err = response.body()?.error?.message ?: "خطا در ثبت‌نام"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
