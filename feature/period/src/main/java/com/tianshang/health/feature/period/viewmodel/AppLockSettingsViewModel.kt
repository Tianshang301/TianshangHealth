package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.security.auth.AppLockManager
import com.tianshang.health.core.security.auth.BiometricAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppLockSettingsState(
    val isEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val lockDelayMs: Long = 0L,
    val showChangePinScreen: Boolean = false
)

@HiltViewModel
class AppLockSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AppLockSettingsState())
    val state: StateFlow<AppLockSettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            AppLockManager.isEnabled.collect { enabled ->
                _state.value = _state.value.copy(
                    isEnabled = enabled,
                    isBiometricEnabled = BiometricAuthManager.isBiometricEnabled(context),
                    lockDelayMs = AppLockManager.getLockDelayMs()
                )
            }
        }
    }

    fun toggleLock(enabled: Boolean) {
        if (enabled) {
            AppLockManager.enable()
        } else {
            AppLockManager.disable()
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        BiometricAuthManager.setBiometricEnabled(context, enabled)
        _state.value = _state.value.copy(isBiometricEnabled = enabled)
    }

    fun setLockDelay(delayMs: Long) {
        AppLockManager.setLockDelayMs(delayMs)
        _state.value = _state.value.copy(lockDelayMs = delayMs)
    }

    fun showChangePin() {
        _state.value = _state.value.copy(showChangePinScreen = true)
    }

    fun hideChangePin() {
        _state.value = _state.value.copy(showChangePinScreen = false)
    }
}
