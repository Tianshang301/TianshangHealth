package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.security.auth.AppLockManager
import com.tianshang.health.core.security.auth.Argon2Hasher
import com.tianshang.health.core.security.encryption.KeystoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppLockState(
    val isAuthenticated: Boolean = false,
    val isPasswordSet: Boolean = false,
    val enteredPin: String = "",
    val mode: LockMode = LockMode.UNLOCK,
    val error: String? = null,
    val failedAttempts: Int = 0,
    val lockoutUntil: Long = 0L
)

enum class LockMode {
    UNLOCK,
    CREATE,
    CONFIRM,
    CHANGE_OLD,
    CHANGE_NEW,
    CHANGE_CONFIRM
}

@HiltViewModel
class AppLockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stringResolver: StringResolver
) : ViewModel() {

    companion object {
        private const val MAX_ATTEMPTS_BEFORE_LOCKOUT = 5
        private const val LOCKOUT_BASE_SECONDS = 30L
        private const val PREF_FAILED_ATTEMPTS = "f3a7b9"
        private const val PREF_LOCKOUT_UNTIL = "l2c5d8"
        private const val PREF_PASSWORD_HASH = "h5n1p8"
        private const val PREF_PASSWORD_SALT = "s2v4x7"
    }

    private val _state = MutableStateFlow(AppLockState())
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    private var pendingNewPin: String = ""

    init {
        checkPasswordStatus()
        loadLockoutState()
    }

    override fun onCleared() {
        super.onCleared()
        pendingNewPin = ""
    }

    private fun checkPasswordStatus() {
        val hasPassword = AppLockManager.isPasswordSet()
        _state.value = _state.value.copy(
            isPasswordSet = hasPassword,
            mode = if (hasPassword) LockMode.UNLOCK else LockMode.CREATE
        )
    }

    private fun loadLockoutState() {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        val failed = prefs.getInt(PREF_FAILED_ATTEMPTS, 0)
        val lockoutUntil = prefs.getLong(PREF_LOCKOUT_UNTIL, 0L)
        _state.value = _state.value.copy(failedAttempts = failed, lockoutUntil = lockoutUntil)
    }

    private fun persistLockoutState(failed: Int, lockoutUntil: Long) {
        val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
        prefs.edit()
            .putInt(PREF_FAILED_ATTEMPTS, failed)
            .putLong(PREF_LOCKOUT_UNTIL, lockoutUntil)
            .apply()
    }

    private fun isCurrentlyLockedOut(): Boolean {
        return System.currentTimeMillis() < _state.value.lockoutUntil
    }

    private fun getLockoutRemainingSeconds(): Long {
        val remaining = (_state.value.lockoutUntil - System.currentTimeMillis()) / 1000
        return remaining.coerceAtLeast(0)
    }

    private fun recordFailedAttempt() {
        val newFailed = _state.value.failedAttempts + 1
        val lockoutUntil = if (newFailed >= MAX_ATTEMPTS_BEFORE_LOCKOUT) {
            val exponent = (newFailed - MAX_ATTEMPTS_BEFORE_LOCKOUT).coerceAtMost(4)
            val delaySeconds = LOCKOUT_BASE_SECONDS * (1L shl exponent)
            System.currentTimeMillis() + delaySeconds * 1000
        } else {
            0L
        }
        _state.value = _state.value.copy(failedAttempts = newFailed, lockoutUntil = lockoutUntil)
        persistLockoutState(newFailed, lockoutUntil)
    }

    private fun resetFailedAttempts() {
        _state.value = _state.value.copy(failedAttempts = 0, lockoutUntil = 0L)
        persistLockoutState(0, 0L)
    }

    fun onDigit(digit: Char) {
        val current = _state.value.enteredPin
        if (current.length >= 6) return
        _state.value = _state.value.copy(
            enteredPin = current + digit,
            error = null
        )
    }

    fun onDelete() {
        val current = _state.value.enteredPin
        if (current.isNotEmpty()) {
            _state.value = _state.value.copy(
                enteredPin = current.dropLast(1)
            )
        }
    }

    fun onClear() {
        _state.value = _state.value.copy(enteredPin = "", error = null)
    }

    fun onConfirm() {
        val pin = _state.value.enteredPin
        if (pin.length < 4) {
            _state.value = _state.value.copy(
                error = stringResolver.getString(R.string.pin_length_error, 4)
            )
            return
        }
        if (_state.value.mode == LockMode.UNLOCK && isCurrentlyLockedOut()) {
            val remaining = getLockoutRemainingSeconds()
            _state.value = _state.value.copy(
                enteredPin = "",
                error = stringResolver.getString(R.string.pin_lockout_error, remaining)
            )
            return
        }
        when (_state.value.mode) {
            LockMode.UNLOCK -> verifyPassword(pin)
            LockMode.CREATE -> {
                pendingNewPin = pin
                _state.value = _state.value.copy(enteredPin = "", mode = LockMode.CONFIRM)
            }
            LockMode.CONFIRM -> {
                if (pin == pendingNewPin) {
                    setPassword(pin)
                } else {
                    _state.value = _state.value.copy(
                        enteredPin = "",
                        mode = LockMode.CREATE,
                        error = stringResolver.getString(R.string.pin_mismatch_error)
                    )
                }
            }
            LockMode.CHANGE_OLD -> verifyPasswordForChange(pin)
            LockMode.CHANGE_NEW -> {
                pendingNewPin = pin
                _state.value = _state.value.copy(enteredPin = "", mode = LockMode.CHANGE_CONFIRM)
            }
            LockMode.CHANGE_CONFIRM -> {
                if (pin == pendingNewPin) {
                    setPassword(pin)
                } else {
                    _state.value = _state.value.copy(
                        enteredPin = "",
                        mode = LockMode.CHANGE_NEW,
                        error = stringResolver.getString(R.string.pin_mismatch_error)
                    )
                }
            }
        }
    }

    fun onRecoverySuccess() {
        pendingNewPin = ""
        resetFailedAttempts()
        _state.value = _state.value.copy(
            enteredPin = "",
            mode = LockMode.CREATE,
            error = null,
            isPasswordSet = false
        )
    }

    fun canUseBiometricRecovery(): Boolean {
        return _state.value.failedAttempts >= 3
    }

    fun startChangePin() {
        onClear()
        _state.value = _state.value.copy(mode = LockMode.CHANGE_OLD)
    }

    fun cancelChangePin() {
        onClear()
        _state.value = _state.value.copy(mode = LockMode.UNLOCK)
    }

    fun setInitialMode(mode: LockMode) {
        if (_state.value.mode != mode) {
            _state.value = _state.value.copy(mode = mode, enteredPin = "", error = null)
        }
    }

    fun onUnlocked() {
        AppLockManager.unlock()
        _state.value = _state.value.copy(isAuthenticated = true)
    }

    private fun setPassword(password: String) {
        viewModelScope.launch {
            try {
                val (salt, hash) = Argon2Hasher.hashPassword(password)
                val saltHex = bytesToHex(salt)
                val hashHex = bytesToHex(hash)
                salt.fill(0)
                hash.fill(0)
                val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
                prefs.edit()
                    .putString(PREF_PASSWORD_SALT, saltHex)
                    .putString(PREF_PASSWORD_HASH, hashHex)
                    .apply()
                _state.value = _state.value.copy(
                    isPasswordSet = true,
                    isAuthenticated = true,
                    mode = LockMode.UNLOCK,
                    enteredPin = "",
                    error = null
                )
                if (!AppLockManager.isEnabled.value) {
                    AppLockManager.enable()
                }
                AppLockManager.unlock()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = stringResolver.getString(R.string.error_failed_set_password)
                )
            }
        }
    }

    private fun verifyPassword(password: String) {
        viewModelScope.launch {
            try {
                val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
                val saltHex = prefs.getString(PREF_PASSWORD_SALT, null)
                val hashHex = prefs.getString(PREF_PASSWORD_HASH, null)
                if (saltHex == null || hashHex == null) {
                    _state.value = _state.value.copy(
                        error = stringResolver.getString(R.string.error_password_not_set)
                    )
                    return@launch
                }
                val salt = hexToBytes(saltHex)
                val expectedHash = hexToBytes(hashHex)
                if (Argon2Hasher.verify(password, salt, expectedHash)) {
                    resetFailedAttempts()
                    AppLockManager.unlock()
                    _state.value = _state.value.copy(
                        isAuthenticated = true,
                        enteredPin = "",
                        error = null
                    )
                } else {
                    recordFailedAttempt()
                    _state.value = _state.value.copy(
                        enteredPin = "",
                        error = stringResolver.getString(R.string.error_incorrect_password)
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
                _state.value = _state.value.copy(
                    enteredPin = "",
                    error = stringResolver.getString(R.string.error_verification_failed)
                )
            }
        }
    }

    private fun verifyPasswordForChange(password: String) {
        viewModelScope.launch {
            try {
                val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
                val saltHex = prefs.getString(PREF_PASSWORD_SALT, null)
                val hashHex = prefs.getString(PREF_PASSWORD_HASH, null)
                if (saltHex == null || hashHex == null) {
                    _state.value = _state.value.copy(
                        error = stringResolver.getString(R.string.error_password_not_set)
                    )
                    return@launch
                }
                val salt = hexToBytes(saltHex)
                val expectedHash = hexToBytes(hashHex)
                if (Argon2Hasher.verify(password, salt, expectedHash)) {
                    _state.value = _state.value.copy(
                        enteredPin = "",
                        mode = LockMode.CHANGE_NEW,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        enteredPin = "",
                        error = stringResolver.getString(R.string.error_incorrect_password)
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.value = _state.value.copy(
                    enteredPin = "",
                    error = stringResolver.getString(R.string.error_verification_failed)
                )
            }
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hexToBytes(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
