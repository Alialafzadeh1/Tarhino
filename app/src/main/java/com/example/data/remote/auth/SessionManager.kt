package com.example.data.remote.auth

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.BuildConfig
import com.example.data.remote.model.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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
 * Secure Session Manager with Android Keystore-backed AES-256-GCM encryption.
 * Encrypts sensitive auth tokens (access_token, refresh_token) before storing in private app storage.
 * Tokens are never logged, never included in analytics, and never exposed in crash reports.
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tarhinoo_auth_session", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    companion object {
        private const val KEY_ACCESS_TOKEN_ENC = "access_token_enc"
        private const val KEY_REFRESH_TOKEN_ENC = "refresh_token_enc"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_BIO = "bio"

        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "tarhinoo_session_token_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SEPARATOR = "]]IV_SEP[["
    }

    private val keyStore: KeyStore? = try {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    } catch (_: Exception) {
        null
    }

    init {
        initKeyStoreKey()
        restoreSession()
    }

    private fun initKeyStoreKey() {
        try {
            if (keyStore != null && !keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {
            // AndroidKeyStore may not be available in non-standard JVM unit test runner; handled gracefully
        }
    }

    private fun encryptToken(plainText: String?): String? {
        if (plainText.isNullOrEmpty()) return null
        return try {
            val key = keyStore?.getKey(KEY_ALIAS, null) as? SecretKey
            if (key != null) {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val iv = cipher.iv
                val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
                val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
                val encBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
                "$ivBase64$IV_SEPARATOR$encBase64"
            } else {
                // Non-standard JVM test runner fallback ONLY in DEBUG
                if (BuildConfig.DEBUG) {
                    "raw:" + Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                } else {
                    // RELEASE: Keystore failure is fatal — never store unencrypted or raw tokens
                    null
                }
            }
        } catch (_: Exception) {
            if (BuildConfig.DEBUG) {
                "raw:" + Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            } else {
                null
            }
        }
    }

    private fun decryptToken(encryptedString: String?): String? {
        if (encryptedString.isNullOrEmpty()) return null
        return try {
            if (encryptedString.startsWith("raw:")) {
                if (BuildConfig.DEBUG) {
                    val base64 = encryptedString.removePrefix("raw:")
                    String(Base64.decode(base64, Base64.NO_WRAP), Charsets.UTF_8)
                } else {
                    // In RELEASE, reject raw fallback tokens
                    null
                }
            } else if (encryptedString.contains(IV_SEPARATOR)) {
                val parts = encryptedString.split(IV_SEPARATOR)
                val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                val cipherBytes = Base64.decode(parts[1], Base64.NO_WRAP)
                val key = keyStore?.getKey(KEY_ALIAS, null) as? SecretKey
                if (key != null) {
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
                    String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
                } else {
                    null
                }
            } else {
                // Legacy unencrypted token migration allowed ONLY in DEBUG
                if (BuildConfig.DEBUG) encryptedString else null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun restoreSession() {
        val encryptedAccessToken = prefs.getString(KEY_ACCESS_TOKEN_ENC, null)
            ?: prefs.getString("access_token", null) // Check legacy key
        val accessToken = decryptToken(encryptedAccessToken)
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
        val encryptedAccessToken = encryptToken(accessToken)
        val encryptedRefreshToken = encryptToken(refreshToken)

        // If encryption failed (e.g. in Release build Keystore fault), fail securely
        if (encryptedAccessToken.isNullOrBlank() || encryptedRefreshToken.isNullOrBlank()) {
            clearSession()
            setAuthError("SECURE_STORAGE_ERROR", "عدم امکان ذخیره‌سازی امن توکن در Keystore دستگاه.")
            return
        }

        prefs.edit()
            .putString(KEY_ACCESS_TOKEN_ENC, encryptedAccessToken)
            .putString(KEY_REFRESH_TOKEN_ENC, encryptedRefreshToken)
            .remove("access_token") // Clear unencrypted legacy tokens if any
            .remove("refresh_token")
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_AVATAR_URL, user.avatarUrl)
            .putString(KEY_BIO, user.bio)
            .apply()

        _authState.value = AuthState.Authenticated(user = user, token = accessToken)
    }

    fun getAccessToken(): String? {
        val enc = prefs.getString(KEY_ACCESS_TOKEN_ENC, null)
            ?: prefs.getString("access_token", null)
        return decryptToken(enc)
    }

    fun getRefreshToken(): String? {
        val enc = prefs.getString(KEY_REFRESH_TOKEN_ENC, null)
            ?: prefs.getString("refresh_token", null)
        return decryptToken(enc)
    }

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
