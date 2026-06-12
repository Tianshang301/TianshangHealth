package com.tianshang.health.feature.steps.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.feature.steps.data.repository.StepsRepository
import com.tianshang.health.feature.steps.service.StepCounterService
import com.tianshang.health.feature.steps.service.StepSyncWorker
import com.tianshang.health.feature.steps.util.OemType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StepsState(
    val todaySteps: Int = 0,
    val todayGoal: Int = HealthConstants.DEFAULT_STEPS_GOAL,
    val weeklySteps: List<DailySteps> = emptyList(),
    val weeklyAverage: Float = 0f,
    val totalStepsThisWeek: Int = 0,
    val isServiceRunning: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val oemType: OemType = OemType.OTHER
)

sealed class StepsUiState {
    object Loading : StepsUiState()
    data class Success(val stepsState: StepsState) : StepsUiState()
    data class Error(val message: String) : StepsUiState()
}

@HiltViewModel
class StepsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stringResolver: StringResolver,
    private val stepsRepository: StepsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StepsUiState>(StepsUiState.Loading)
    val uiState: StateFlow<StepsUiState> = _uiState.asStateFlow()

    private var observeJob: kotlinx.coroutines.Job? = null

    init {
        startStepCounter()
        observeSteps()
    }

    private fun observeSteps() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            try {
                stepsRepository.initialize()
                collectSteps()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = StepsUiState.Error(e.message ?: stringResolver.getString(R.string.error_unknown))
            }
        }
    }

    private suspend fun collectSteps() {
        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isBatteryOptimizationDisabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
        val oemType = OemType.detect()

        combine(
            stepsRepository.observeTodaySteps(),
            stepsRepository.getTodaySteps(),
            stepsRepository.getStepsByDateRange(weekStart, today)
        ) { count, todaySteps, weeklySteps ->
            val todayGoal = todaySteps?.goal ?: HealthConstants.DEFAULT_STEPS_GOAL
            val totalStepsThisWeek = weeklySteps.sumOf { it.count }
            val weeklyAverage = if (weeklySteps.isNotEmpty()) {
                totalStepsThisWeek.toFloat() / weeklySteps.size
            } else {
                0f
            }

            StepsState(
                todaySteps = count,
                todayGoal = todayGoal,
                weeklySteps = weeklySteps,
                weeklyAverage = weeklyAverage,
                totalStepsThisWeek = totalStepsThisWeek,
                isServiceRunning = true,
                isBatteryOptimizationDisabled = isBatteryOptimizationDisabled,
                oemType = oemType
            )
        }.collect { state ->
            _uiState.value = StepsUiState.Success(state)
        }
    }

    private fun startStepCounter() {
        StepCounterService.startService(context)
        StepSyncWorker.schedule(context)
    }

    fun requestDisableBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openOemBatterySettings() {
        val oemType = OemType.detect()
        oemType.openBatterySettings(context)
    }

    fun openOemAutoStartSettings() {
        val oemType = OemType.detect()
        oemType.openAutoStartSettings(context)
    }

    fun updateGoal(goal: Int) {
        viewModelScope.launch {
            try {
                stepsRepository.updateGoal(goal)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = StepsUiState.Error(e.message ?: stringResolver.getString(R.string.error_failed_update_goal))
            }
        }
    }

    fun refresh() {
        observeSteps()
    }

    override fun onCleared() {
        super.onCleared()
    }
}
