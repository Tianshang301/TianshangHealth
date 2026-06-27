package com.tianshang.health.feature.fitness.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.core.database.entity.WorkoutRecord
import com.tianshang.health.feature.fitness.data.repository.FitnessRepository
import com.tianshang.health.feature.fitness.domain.CycleFitnessResult
import com.tianshang.health.feature.fitness.domain.GetCycleFitnessRecommendationsUseCase
import com.tianshang.health.feature.fitness.util.CalorieCalculator
import com.tianshang.health.feature.fitness.util.ExerciseType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FitnessState(
    val todayWorkouts: List<WorkoutRecord> = emptyList(),
    val recentWorkouts: List<WorkoutRecord> = emptyList(),
    val totalDurationThisWeek: Int = 0,
    val totalCaloriesThisWeek: Float = 0f,
    val workoutCountThisWeek: Int = 0,
    val totalCaloriesToday: Float = 0f,
    val todaySteps: Int = 0,
    val todayStepsCalories: Float = 0f,
    val todayGoal: Int = HealthConstants.DEFAULT_STEPS_GOAL,
    val combinedDailyCalories: Float = 0f,
    val weeklySteps: List<DailySteps> = emptyList(),
    val totalStepsThisWeek: Int = 0,
    val weeklyStepsCalories: Float = 0f,
    val totalStepsAllTime: Int = 0,
    val userHeight: Float? = null,
    val userLatestWeight: Float? = null,
    val weightInput: String = "",
    val heightInput: String = "",
    val weightInputError: String? = null,
    val heightInputError: String? = null,
    val saveBodyMetricsSuccess: Boolean = false,
    val cycleFitnessResult: CycleFitnessResult = CycleFitnessResult.EMPTY,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class AddWorkoutState(
    val selectedDate: String = LocalDate.now().toString(),
    val selectedType: ExerciseType = ExerciseType.RUNNING,
    val durationMinutes: String = "",
    val caloriesBurned: String = "",
    val distanceMeters: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val autoCaloriesDisplay: String? = null,
    val caloriesManuallyOverridden: Boolean = false
)

sealed class FitnessUiState {
    object Loading : FitnessUiState()
    data class Success(val state: FitnessState) : FitnessUiState()
    data class Error(val message: String) : FitnessUiState()
}

@HiltViewModel
class FitnessViewModel @Inject constructor(
    private val stringResolver: StringResolver,
    private val repository: FitnessRepository,
    private val getCycleFitnessRecommendations: GetCycleFitnessRecommendationsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FitnessState())
    val state: StateFlow<FitnessState> = _state.asStateFlow()

    private val _addState = MutableStateFlow(AddWorkoutState())
    val addState: StateFlow<AddWorkoutState> = _addState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                repository.initialize()
                val today = LocalDate.now().toString()

                repository.getWorkoutsByDate(today).collect { workouts ->
                    _state.update {
                        it.copy(todayWorkouts = workouts)
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = stringResolver.getString(R.string.error_unknown)) }
            }
        }

        viewModelScope.launch {
            try {
                val totalDuration = repository.getTotalDurationThisWeek()
                val totalCalories = repository.getTotalCaloriesThisWeek()
                val workoutCount = repository.getWorkoutCountThisWeek()
                val today = LocalDate.now().toString()
                val todayCalories = repository.getTotalCaloriesByDate(today)
                val todaySteps = repository.getTodaySteps()
                val todayStepsCalories = repository.getTodayStepsCalories()
                val combinedDailyCalories = repository.getCombinedDailyCalories(today)
                val userHeight = repository.getUserHeight()
                val userLatestWeight = repository.getLatestWeight()
                val totalStepsThisWeek = repository.getTotalStepsThisWeek()
                val weeklyStepsCalories = CalorieCalculator.stepsToKcal(
                    totalStepsThisWeek,
                    userLatestWeight,
                    userHeight
                )

                _state.update {
                    it.copy(
                        totalDurationThisWeek = totalDuration,
                        totalCaloriesThisWeek = totalCalories,
                        workoutCountThisWeek = workoutCount,
                        totalCaloriesToday = todayCalories,
                        todaySteps = todaySteps,
                        todayStepsCalories = todayStepsCalories,
                        combinedDailyCalories = combinedDailyCalories,
                        userHeight = userHeight,
                        userLatestWeight = userLatestWeight,
                        totalStepsThisWeek = totalStepsThisWeek,
                        weeklyStepsCalories = weeklyStepsCalories,
                        heightInput = userHeight?.let { String.format("%.1f", it) } ?: "",
                        weightInput = userLatestWeight?.let { String.format("%.1f", it) } ?: "",
                        isLoading = false
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = stringResolver.getString(R.string.error_unknown)) }
            }
        }

        viewModelScope.launch {
            try {
                repository.getAllWorkouts().collect { workouts ->
                    _state.update {
                        it.copy(recentWorkouts = workouts.take(20))
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("FitnessViewModel", "Failed to collect workouts", e)
            }
        }

        viewModelScope.launch {
            try {
                val result = getCycleFitnessRecommendations()
                _state.update { it.copy(cycleFitnessResult = result) }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("FitnessViewModel", "Failed to get cycle fitness", e)
            }
        }
    }

    fun updateSelectedDate(date: String) {
        _addState.update { it.copy(selectedDate = date) }

        viewModelScope.launch {
            try {
                repository.getWorkoutsByDate(date).collect { workouts ->
                    _state.update { it.copy(todayWorkouts = workouts) }
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("FitnessViewModel", "Failed to collect workouts by date", e)
            }
        }
        refreshDailyCalories(date)
    }

    private fun refreshDailyCalories(date: String) {
        viewModelScope.launch {
            try {
                val todayCalories = repository.getTotalCaloriesByDate(date)
                _state.update { it.copy(totalCaloriesToday = todayCalories) }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("FitnessViewModel", "Failed to refresh daily calories", e)
            }
        }
    }

    fun updateSelectedType(type: ExerciseType) {
        _addState.update { it.copy(selectedType = type) }
        recalculateCalories()
    }

    fun updateDuration(duration: String) {
        _addState.update { it.copy(durationMinutes = duration) }
        if (!_addState.value.caloriesManuallyOverridden) {
            recalculateCalories()
        }
    }

    fun updateCalories(calories: String) {
        val isManualOverride = calories.isNotBlank()
        _addState.update {
            it.copy(
                caloriesBurned = calories,
                caloriesManuallyOverridden = isManualOverride,
                autoCaloriesDisplay = if (isManualOverride) null else it.autoCaloriesDisplay
            )
        }
    }

    private fun recalculateCalories() {
        val state = _addState.value
        val duration = state.durationMinutes.toIntOrNull()
        if (duration == null || duration <= 0) {
            _addState.update { it.copy(autoCaloriesDisplay = null) }
            return
        }
        viewModelScope.launch {
            try {
                val kcal = repository.autoCalculateCalories(
                    state.selectedType.value,
                    duration,
                    state.selectedDate
                )
                val (kcalStr, kjStr) = CalorieCalculator.formatBoth(kcal)
                _addState.update {
                    it.copy(
                        caloriesBurned = String.format("%.0f", kcal),
                        autoCaloriesDisplay = "$kcalStr ($kjStr)"
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("FitnessViewModel", "Failed to auto-calculate calories", e)
            }
        }
    }

    fun updateDistance(distance: String) {
        _addState.update { it.copy(distanceMeters = distance) }
    }

    fun updateNotes(notes: String) {
        _addState.update { it.copy(notes = notes) }
    }

    fun saveWorkout() {
        val addState = _addState.value
        val duration = addState.durationMinutes.toIntOrNull()
        if (duration == null || duration <= 0) {
            _addState.update { it.copy(error = stringResolver.getString(R.string.error_valid_duration)) }
            return
        }

        viewModelScope.launch {
            _addState.update { it.copy(isSaving = true, error = null) }
            try {
                val calories = addState.caloriesBurned.toFloatOrNull()
                val distance = addState.distanceMeters.toFloatOrNull()

                repository.addWorkout(
                    exerciseType = addState.selectedType.value,
                    durationMinutes = duration,
                    caloriesBurned = calories,
                    distanceMeters = distance,
                    notes = addState.notes.ifBlank { null },
                    date = addState.selectedDate
                )
                refreshDailyCalories(addState.selectedDate)
                refreshWeeklyStats()
                refreshCombinedStats()
                _addState.update {
                    AddWorkoutState(
                        selectedDate = it.selectedDate,
                        saveSuccess = true
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _addState.update { it.copy(isSaving = false, error = stringResolver.getString(R.string.error_unknown)) }
            }
        }
    }

    fun resetAddState() {
        _addState.value = AddWorkoutState(selectedDate = _addState.value.selectedDate)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
        _addState.update { it.copy(error = null) }
    }

    fun updateWeightInput(value: String) {
        _state.update { it.copy(weightInput = value, weightInputError = null) }
    }

    fun updateHeightInput(value: String) {
        _state.update { it.copy(heightInput = value, heightInputError = null) }
    }

    fun saveBodyMetrics() {
        viewModelScope.launch {
            try {
                val weightStr = _state.value.weightInput
                val heightStr = _state.value.heightInput
                var hasError = false

                if (heightStr.isNotBlank()) {
                    val heightCm = heightStr.toFloatOrNull()
                    if (heightCm == null || heightCm < 80f || heightCm > 250f) {
                        _state.update {
                            it.copy(
                                heightInputError = stringResolver.getString(R.string.body_metrics_height_error)
                            )
                        }
                        hasError = true
                    } else {
                        _state.update { it.copy(heightInputError = null) }
                        repository.updateHeight(heightCm)
                    }
                }

                if (weightStr.isNotBlank()) {
                    val weightKg = weightStr.toFloatOrNull()
                    if (weightKg == null || weightKg < 15f || weightKg > 350f) {
                        _state.update {
                            it.copy(
                                weightInputError = stringResolver.getString(R.string.body_metrics_weight_error)
                            )
                        }
                        hasError = true
                    } else {
                        _state.update { it.copy(weightInputError = null) }
                        repository.saveWeight(weightKg, LocalDate.now().toString())
                    }
                }

                if (!hasError) {
                    _state.update { it.copy(saveBodyMetricsSuccess = true) }
                    refreshBodyMetrics()
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(error = stringResolver.getString(R.string.error_unknown)) }
            }
        }
    }

    fun clearBodyMetricsSuccess() {
        _state.update { it.copy(saveBodyMetricsSuccess = false) }
    }

    private suspend fun refreshBodyMetrics() {
        try {
            val height = repository.getUserHeight()
            val weight = repository.getLatestWeight()
            _state.update {
                it.copy(
                    userHeight = height,
                    userLatestWeight = weight,
                    heightInput = height?.let { String.format("%.1f", it) } ?: "",
                    weightInput = weight?.let { String.format("%.1f", it) } ?: "",
                    heightInputError = null,
                    weightInputError = null
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.e("FitnessViewModel", "Failed to refresh body metrics", e)
        }
    }

    private suspend fun refreshCombinedStats() {
        try {
            val today = LocalDate.now().toString()
            val todaySteps = repository.getTodaySteps()
            val todayStepsCalories = repository.getTodayStepsCalories()
            val combinedDailyCalories = repository.getCombinedDailyCalories(today)
            val todayCalories = repository.getTotalCaloriesByDate(today)
            _state.update {
                it.copy(
                    todaySteps = todaySteps,
                    todayStepsCalories = todayStepsCalories,
                    combinedDailyCalories = combinedDailyCalories,
                    totalCaloriesToday = todayCalories
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.e("FitnessViewModel", "Failed to refresh combined stats", e)
        }
    }

    fun deleteWorkout(record: WorkoutRecord) {
        viewModelScope.launch {
            try {
                repository.deleteWorkout(record)
                refreshDailyCalories(record.date)
                refreshWeeklyStats()
                refreshCombinedStats()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(error = stringResolver.getString(R.string.error_failed_delete)) }
            }
        }
    }

    private fun refreshWeeklyStats() {
        viewModelScope.launch {
            try {
                val totalDuration = repository.getTotalDurationThisWeek()
                val totalCalories = repository.getTotalCaloriesThisWeek()
                val workoutCount = repository.getWorkoutCountThisWeek()
                _state.update {
                    it.copy(
                        totalDurationThisWeek = totalDuration,
                        totalCaloriesThisWeek = totalCalories,
                        workoutCountThisWeek = workoutCount
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("FitnessViewModel", "Failed to refresh weekly stats", e)
            }
        }
    }
}
