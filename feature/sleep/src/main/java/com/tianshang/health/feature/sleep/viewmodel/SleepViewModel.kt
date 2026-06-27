package com.tianshang.health.feature.sleep.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.feature.sleep.data.repository.SleepRepository
import com.tianshang.health.feature.sleep.data.repository.SleepRepository.SleepConsistencyScore
import com.tianshang.health.feature.sleep.domain.HealthInsight
import com.tianshang.health.feature.sleep.domain.SleepInsightEngine
import com.tianshang.health.feature.sleep.domain.SleepQualityAnalyzer
import com.tianshang.health.feature.sleep.domain.SleepQualityIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SleepState(
    val todaySleep: DailyHealth? = null,
    val recentSleep: List<DailyHealth> = emptyList(),
    val hoursInput: String = "",
    val deepHoursInput: String = "",
    val qualityInput: Int? = null,
    val selectedDate: String = LocalDate.now().toString(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val bedTimeInput: String = "",
    val wakeTimeInput: String = "",
    val sleepLatencyInput: String = "",
    val wakeCountInput: String = "",
    val consistencyScore: SleepConsistencyScore? = null,
    val qualityIndex: SleepQualityIndex? = null,
    val insights: List<HealthInsight> = emptyList()
)

@HiltViewModel
class SleepViewModel @Inject constructor(
    private val stringResolver: StringResolver,
    private val repository: SleepRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SleepState())
    val state: StateFlow<SleepState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                repository.initialize()
                val today = LocalDate.now().toString()
                val todayData = repository.getTodaySleep(today)
                val recent = repository.getRecentDays(7)
                val consistency = repository.getSleepConsistency(14)
                val longer = repository.getRecentDays(14)
                val analyzer = SleepQualityAnalyzer()
                val insightEngine = SleepInsightEngine()
                val qIndex = analyzer.computeQualityIndex(longer)
                val ins = insightEngine.generateInsights(longer, qIndex)
                _state.update {
                    it.copy(
                        todaySleep = todayData,
                        recentSleep = recent,
                        hoursInput = todayData?.sleepHours?.toString() ?: "",
                        deepHoursInput = todayData?.deepSleepHours?.toString() ?: "",
                        qualityInput = todayData?.sleepQuality,
                        bedTimeInput = todayData?.bedTime ?: "",
                        wakeTimeInput = todayData?.wakeTime ?: "",
                        sleepLatencyInput = todayData?.sleepLatency?.toString() ?: "",
                        wakeCountInput = todayData?.wakeCount?.toString() ?: "",
                        consistencyScore = consistency,
                        qualityIndex = qIndex,
                        insights = ins,
                        isLoading = false
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = stringResolver.getString(R.string.error_unknown)) }
            }
        }
    }

    fun updateHours(hours: String) {
        _state.update { it.copy(hoursInput = hours) }
    }

    fun updateDeepHours(hours: String) {
        _state.update { it.copy(deepHoursInput = hours) }
    }

    fun updateQuality(quality: Int) {
        _state.update { it.copy(qualityInput = quality) }
    }

    fun updateBedTime(value: String) {
        _state.update { it.copy(bedTimeInput = value) }
    }

    fun updateWakeTime(value: String) {
        _state.update { it.copy(wakeTimeInput = value) }
    }

    fun updateSleepLatency(value: String) {
        _state.update { it.copy(sleepLatencyInput = value) }
    }

    fun updateWakeCount(value: String) {
        _state.update { it.copy(wakeCountInput = value) }
    }

    fun updateDate(date: String) {
        _state.update { it.copy(selectedDate = date) }
        viewModelScope.launch {
            try {
                val dayData = repository.getTodaySleep(date)
                _state.update {
                    it.copy(
                        todaySleep = dayData,
                        hoursInput = dayData?.sleepHours?.toString() ?: "",
                        deepHoursInput = dayData?.deepSleepHours?.toString() ?: "",
                        qualityInput = dayData?.sleepQuality,
                        bedTimeInput = dayData?.bedTime ?: "",
                        wakeTimeInput = dayData?.wakeTime ?: "",
                        sleepLatencyInput = dayData?.sleepLatency?.toString() ?: "",
                        wakeCountInput = dayData?.wakeCount?.toString() ?: ""
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("SleepViewModel", "Failed to load sleep data", e)
            }
        }
    }

    fun saveSleep() {
        val current = _state.value
        viewModelScope.launch {
            try {
                _state.update { it.copy(isSaving = true, error = null) }
                val hours = current.hoursInput.toFloatOrNull()
                val deepHours = current.deepHoursInput.toFloatOrNull()
                repository.saveSleep(
                    date = current.selectedDate,
                    sleepHours = hours,
                    deepSleepHours = deepHours,
                    sleepQuality = current.qualityInput,
                    bedTime = current.bedTimeInput.ifBlank { null },
                    wakeTime = current.wakeTimeInput.ifBlank { null },
                    sleepLatency = current.sleepLatencyInput.toIntOrNull(),
                    wakeCount = current.wakeCountInput.toIntOrNull()
                )
                val recent = repository.getRecentDays(7)
                val todayData = repository.getTodaySleep(current.selectedDate)
                val consistency = repository.getSleepConsistency(14)
                val longer = repository.getRecentDays(14)
                val analyzer = SleepQualityAnalyzer()
                val insightEngine = SleepInsightEngine()
                val qIndex = analyzer.computeQualityIndex(longer)
                val ins = insightEngine.generateInsights(longer, qIndex)
                _state.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        todaySleep = todayData,
                        recentSleep = recent,
                        consistencyScore = consistency,
                        qualityIndex = qIndex,
                        insights = ins
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = stringResolver.getString(R.string.error_unknown)) }
            }
        }
    }

    fun clearSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
