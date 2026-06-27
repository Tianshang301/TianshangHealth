package com.tianshang.health.core.security.auth

import android.content.Context
import com.tianshang.health.core.security.encryption.KeystoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

object AppLockManager {

    private const val PREF_LOCK_ENABLED = "l7k2m9"
    private const val PREF_LOCK_DELAY = "d4e7g0"
    private const val PREF_LOCK_TIMESTAMP = "t8r3w6"
    private const val PREF_PASSWORD_HASH = "h5n1p8"
    private const val PREF_PASSWORD_SALT = "s2v4x7"

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private var appContextRef: WeakReference<Context>? = null
    private var isInitialized = false

    private val appContext: Context?
        get() = appContextRef?.get()

    fun init(context: Context) {
        if (isInitialized) return
        appContextRef = WeakReference(context.applicationContext)
        val ctx = appContext ?: return
        val prefs = KeystoreManager.getEncryptedSharedPreferences(ctx)
        _isEnabled.value = prefs.getBoolean(PREF_LOCK_ENABLED, false)
        if (_isEnabled.value) {
            _isLocked.value = true
        }
        isInitialized = true
    }

    private fun checkInit() {
        if (!isInitialized) throw IllegalStateException("AppLockManager not initialized. Call init() first.")
    }

    private fun prefs(): android.content.SharedPreferences {
        val ctx = appContext ?: throw IllegalStateException("AppLockManager context reference lost")
        return KeystoreManager.getEncryptedSharedPreferences(ctx)
    }

    fun enable() {
        checkInit()
        prefs().edit().putBoolean(PREF_LOCK_ENABLED, true).apply()
        _isEnabled.value = true
        lock()
    }

    fun disable() {
        checkInit()
        prefs().edit()
            .putBoolean(PREF_LOCK_ENABLED, false)
            .remove(PREF_PASSWORD_HASH)
            .remove(PREF_PASSWORD_SALT)
            .apply()
        _isEnabled.value = false
        _isLocked.value = false
    }

    fun lock() {
        _isLocked.value = true
    }

    fun unlock() {
        _isLocked.value = false
        clearBackgroundTimestamp()
    }

    fun isPasswordSet(): Boolean {
        checkInit()
        val prefs = prefs()
        return prefs.contains(PREF_PASSWORD_HASH) && prefs.contains(PREF_PASSWORD_SALT)
    }

    fun getLockDelayMs(): Long {
        checkInit()
        return prefs().getLong(PREF_LOCK_DELAY, 0L)
    }

    fun setLockDelayMs(delayMs: Long) {
        checkInit()
        prefs().edit().putLong(PREF_LOCK_DELAY, delayMs).apply()
    }

    fun onBackground() {
        checkInit()
        if (!_isEnabled.value) return
        val delayMs = getLockDelayMs()
        if (delayMs <= 0L) {
            lock()
        } else {
            prefs().edit().putLong(PREF_LOCK_TIMESTAMP, System.currentTimeMillis()).apply()
        }
    }

    fun onForeground() {
        checkInit()
        if (!_isEnabled.value) return
        val delayMs = getLockDelayMs()
        if (delayMs <= 0L) return
        val timestamp = prefs().getLong(PREF_LOCK_TIMESTAMP, -1L)
        if (timestamp > 0 && System.currentTimeMillis() - timestamp >= delayMs) {
            lock()
        }
        clearBackgroundTimestamp()
    }

    private fun clearBackgroundTimestamp() {
        prefs().edit().remove(PREF_LOCK_TIMESTAMP).apply()
    }
}
