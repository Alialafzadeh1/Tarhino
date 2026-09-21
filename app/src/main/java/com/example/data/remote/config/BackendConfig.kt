package com.example.data.remote.config

import com.example.BuildConfig

/**
 * Backend connection mode for Tarhi Noo Messenger:
 * - LOCAL_ONLY: No backend configured yet, Room database handles local messaging safely.
 * - BACKEND_CONNECTED: Valid backend credentials provided, remote API and Realtime active.
 * - OFFLINE: Backend configured, but network/realtime is temporarily unreachable.
 */
enum class BackendMode {
    LOCAL_ONLY,
    BACKEND_CONNECTED,
    OFFLINE
}

object BackendConfig {
    /**
     * Resolves backend base URL from BuildConfig (injected via .env).
     * Defaults to empty if not configured.
     */
    val baseUrl: String
        get() = try {
            val field = BuildConfig::class.java.getField("BACKEND_BASE_URL")
            (field.get(null) as? String)?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }

    val realtimeUrl: String
        get() = try {
            val field = BuildConfig::class.java.getField("REALTIME_URL")
            (field.get(null) as? String)?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }

    val storageUrl: String
        get() = try {
            val field = BuildConfig::class.java.getField("STORAGE_URL")
            (field.get(null) as? String)?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }

    val publicAppId: String
        get() = try {
            val field = BuildConfig::class.java.getField("PUBLIC_APP_ID")
            (field.get(null) as? String)?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }

    val isBackendConfigured: Boolean
        get() = baseUrl.isNotBlank() && baseUrl.startsWith("http")

    fun getInitialMode(): BackendMode {
        return if (isBackendConfigured) BackendMode.BACKEND_CONNECTED else BackendMode.LOCAL_ONLY
    }
}
