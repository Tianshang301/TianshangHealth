package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.security.encryption.KeystoreManager
import com.tianshang.health.feature.period.service.OvulationReminderWorker
import com.tianshang.health.feature.period.service.PeriodReminderWorker
import com.tianshang.health.feature.period.service.PmsReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderSettings(
    val periodReminderEnabled: Boolean = false,
    val periodReminderHour: Int = HealthConstants.DEFAULT_PERIOD_REMINDER_HOUR,
    val periodReminderMinute: Int = 0,
    val ovulationReminderEnabled: Boolean = false,
    val ovulationDaysBefore: Int = HealthConstants.DEFAULT_OVULATION_DAYS_BEFORE,
    val pmsReminderEnabled: Boolean = false,
    val pmsDaysBefore: Int = HealthConstants.DEFAULT_PMS_DAYS_BEFORE
)

sealed class ReminderUiState {
    object Loading : ReminderUiState()
    data class Success(val settings: ReminderSettings) : ReminderUiState()
    data class Error(val message: String) : ReminderUiState()
}

@HiltViewModel
class ReminderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stringResolver: StringResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReminderUiState>(ReminderUiState.Loading)
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    private val prefs = KeystoreManager.getEncryptedSharedPreferences(context)

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = ReminderSettings(
                    periodReminderEnabled = prefs.getBoolean("period_reminder_enabled", false),
                    periodReminderHour = prefs.getInt(
                        "period_reminder_hour",
                        HealthConstants.DEFAULT_PERIOD_REMINDER_HOUR
                    ),
                    periodReminderMinute = prefs.getInt("period_reminder_minute", 0),
                    ovulationReminderEnabled = prefs.getBoolean("ovulation_reminder_enabled", false),
                    ovulationDaysBefore = prefs.getInt(
                        "ovulation_days_before",
                        HealthConstants.DEFAULT_OVULATION_DAYS_BEFORE
                    ),
                    pmsReminderEnabled = prefs.getBoolean("pms_reminder_enabled", false),
                    pmsDaysBefore = prefs.getInt("pms_days_before", HealthConstants.DEFAULT_PMS_DAYS_BEFORE)
                )
                _uiState.value = ReminderUiState.Success(settings)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = ReminderUiState.Error(e.message ?: stringResolver.getString(R.string.error_failed_load_settings))
            }
        }
    }

    fun togglePeriodReminder(enabled: Boolean) {
        viewModelScope.launch {
            try {
                prefs.edit().putBoolean("period_reminder_enabled", enabled).apply()

                if (enabled) {
                    val hour = prefs.getInt("period_reminder_hour", HealthConstants.DEFAULT_PERIOD_REMINDER_HOUR)
                    val minute = prefs.getInt("period_reminder_minute", 0)
                    PeriodReminderWorker.scheduleReminder(context, hour, minute)
                } else {
                    PeriodReminderWorker.cancelReminder(context)
                }

                loadSettings()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = ReminderUiState.Error(e.message ?: stringResolver.getString(R.string.error_failed_update_settings))
            }
        }
    }

    fun setPeriodReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                prefs.edit()
                    .putInt("period_reminder_hour", hour)
                    .putInt("period_reminder_minute", minute)
                    .apply()

                if (prefs.getBoolean("period_reminder_enabled", false)) {
                    PeriodReminderWorker.scheduleReminder(context, hour, minute)
                }

                loadSettings()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = ReminderUiState.Error(e.message ?: stringResolver.getString(R.string.error_failed_update_settings))
            }
        }
    }

    fun toggleOvulationReminder(enabled: Boolean) {
        viewModelScope.launch {
            try {
                prefs.edit().putBoolean("ovulation_reminder_enabled", enabled).apply()

                if (enabled) {
                    OvulationReminderWorker.scheduleReminder(context)
                } else {
                    OvulationReminderWorker.cancelReminder(context)
                }

                loadSettings()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = ReminderUiState.Error(e.message ?: stringResolver.getString(R.string.error_failed_update_settings))
            }
        }
    }

    fun setOvulationDaysBefore(days: Int) {
        viewModelScope.launch {
            try {
                prefs.edit().putInt("ovulation_days_before", days).apply()

                if (prefs.getBoolean("ovulation_reminder_enabled", false)) {
                    OvulationReminderWorker.scheduleReminder(context)
                }

                loadSettings()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = ReminderUiState.Error(e.message ?: stringResolver.getString(R.string.error_failed_update_settings))
            }
        }
    }

    fun togglePmsReminder(enabled: Boolean) {
        viewModelScope.launch {
            try {
                prefs.edit().putBoolean("pms_reminder_enabled", enabled).apply()

                if (enabled) {
                    PmsReminderWorker.scheduleReminder(context)
                } else {
                    PmsReminderWorker.cancelReminder(context)
                }

                loadSettings()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = ReminderUiState.Error(e.message ?: stringResolver.getString(R.string.error_failed_update_settings))
            }
        }
    }

    fun setPmsDaysBefore(days: Int) {
        viewModelScope.launch {
            try {
                prefs.edit().putInt("pms_days_before", days).apply()

                if (prefs.getBoolean("pms_reminder_enabled", false)) {
                    PmsReminderWorker.scheduleReminder(context)
                }

                loadSettings()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = ReminderUiState.Error(e.message ?: stringResolver.getString(R.string.error_failed_update_settings))
            }
        }
    }
}
