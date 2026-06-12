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
    val error: String? = null
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

    private val _state = MutableStateFlow(AppLockState())
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    private var pendingNewPin: String = ""

    init {
        checkPasswordStatus()
    }

    private fun checkPasswordStatus() {
        val hasPassword = AppLockManager.isPasswordSet()
        _state.value = _state.value.copy(
            isPasswordSet = hasPassword,
            mode = if (hasPassword) LockMode.UNLOCK else LockMode.CREATE
        )
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
        _state.value = _state.value.copy(
            enteredPin = "",
            mode = LockMode.CREATE,
            error = null,
            isPasswordSet = false
        )
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
                val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
                prefs.edit()
                    .putString("password_salt", bytesToHex(salt))
                    .putString("password_hash", bytesToHex(hash))
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
                val saltHex = prefs.getString("password_salt", null)
                val hashHex = prefs.getString("password_hash", null)
                if (saltHex == null || hashHex == null) {
                    _state.value = _state.value.copy(
                        error = stringResolver.getString(R.string.error_password_not_set)
                    )
                    return@launch
                }
                val salt = hexToBytes(saltHex)
                val expectedHash = hexToBytes(hashHex)
                if (Argon2Hasher.verify(password, salt, expectedHash)) {
                    AppLockManager.unlock()
                    _state.value = _state.value.copy(
                        isAuthenticated = true,
                        enteredPin = "",
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

    private fun verifyPasswordForChange(password: String) {
        viewModelScope.launch {
            try {
                val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
                val saltHex = prefs.getString("password_salt", null)
                val hashHex = prefs.getString("password_hash", null)
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
