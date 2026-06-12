package com.tianshang.health.core.security.auth

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import com.tianshang.health.core.security.encryption.KeystoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

object ScreenshotProtectionManager {

    private const val PREF_SCREENSHOT_PROTECTION_ENABLED = "k7m2p9q4"

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private var appContextRef: WeakReference<Context>? = null
    private var currentActivityRef: WeakReference<Activity>? = null
    private var isInitialized = false

    private val appContext: Context?
        get() = appContextRef?.get()

    fun init(context: Context) {
        if (isInitialized) return
        appContextRef = WeakReference(context.applicationContext)
        val ctx = appContext ?: return
        val prefs = KeystoreManager.getEncryptedSharedPreferences(ctx)
        _isEnabled.value = prefs.getBoolean(PREF_SCREENSHOT_PROTECTION_ENABLED, true)
        isInitialized = true
    }

    fun registerActivity(activity: Activity) {
        currentActivityRef = WeakReference(activity)
        applyToCurrentWindow()
    }

    private fun checkInit() {
        if (!isInitialized) {
            throw IllegalStateException(
                "ScreenshotProtectionManager not initialized. Call init() first."
            )
        }
    }

    private fun prefs(): android.content.SharedPreferences {
        val ctx = appContext ?: throw IllegalStateException("ScreenshotProtectionManager context reference lost")
        return KeystoreManager.getEncryptedSharedPreferences(ctx)
    }

    fun setEnabled(enabled: Boolean) {
        checkInit()
        prefs().edit().putBoolean(PREF_SCREENSHOT_PROTECTION_ENABLED, enabled).apply()
        _isEnabled.value = enabled
        applyToCurrentWindow()
    }

    private fun applyToCurrentWindow() {
        val activity = currentActivityRef?.get() ?: return
        try {
            val window = activity.window
            if (_isEnabled.value) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
        }
    }
}
