package com.example.data.remote.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.model.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object NoSession : AuthState()
    object Loading : AuthState()
    data class Authenticated(
        val user: UserDto,
        val token: String
    ) : AuthState()
    data class SessionExpired(val message: String) : AuthState()
    data class AuthError(val code: String, val message: String) : AuthState()
}

/**
 * Secure Session Manager.
 * Stores tokens securely in private app SharedPreferences.
 * Never logs raw auth tokens.
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tarhinoo_auth_session", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_BIO = "bio"
    }

    init {
        restoreSession()
    }

    fun restoreSession() {
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val userId = prefs.getString(KEY_USER_ID, null)
        val username = prefs.getString(KEY_USERNAME, null)
        val displayName = prefs.getString(KEY_DISPLAY_NAME, null)

        if (!accessToken.isNullOrBlank() && !userId.isNullOrBlank() && !username.isNullOrBlank()) {
            val user = UserDto(
                id = userId,
                username = username,
                displayName = displayName ?: username,
                avatarUrl = prefs.getString(KEY_AVATAR_URL, "") ?: "",
                bio = prefs.getString(KEY_BIO, "") ?: "",
                isOnline = true
            )
            _authState.value = AuthState.Authenticated(user = user, token = accessToken)
        } else {
            _authState.value = AuthState.NoSession
        }
    }

    fun saveSession(
        user: UserDto,
        accessToken: String,
        refreshToken: String,
        expiresInSeconds: Long
    ) {
        val expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_AVATAR_URL, user.avatarUrl)
            .putString(KEY_BIO, user.bio)
            .apply()

        _authState.value = AuthState.Authenticated(user = user, token = accessToken)
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getCurrentUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun isTokenExpired(): Boolean {
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return expiresAt > 0 && System.currentTimeMillis() >= (expiresAt - 60_000)
    }

    fun markSessionExpired(reason: String) {
        _authState.value = AuthState.SessionExpired(reason)
    }

    fun setAuthError(code: String, message: String) {
        _authState.value = AuthState.AuthError(code, message)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _authState.value = AuthState.NoSession
    }
}
