package com.tianshang.health.feature.sleep.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.feature.sleep.data.repository.SleepRepository
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
    val error: String? = null
)

@HiltViewModel
class SleepViewModel @Inject constructor(
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
                _state.update {
                    it.copy(
                        todaySleep = todayData,
                        recentSleep = recent,
                        hoursInput = todayData?.sleepHours?.toString() ?: "",
                        deepHoursInput = todayData?.deepSleepHours?.toString() ?: "",
                        qualityInput = todayData?.sleepQuality,
                        isLoading = false
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
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
                        qualityInput = dayData?.sleepQuality
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
                    sleepQuality = current.qualityInput
                )
                val recent = repository.getRecentDays(7)
                val todayData = repository.getTodaySleep(current.selectedDate)
                _state.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        todaySleep = todayData,
                        recentSleep = recent
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
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
