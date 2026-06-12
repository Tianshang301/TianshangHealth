package com.tianshang.health.feature.nutrition.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.MealRecord
import com.tianshang.health.feature.nutrition.data.repository.CycleNutritionRecommendation
import com.tianshang.health.feature.nutrition.data.repository.NutritionRepository
import com.tianshang.health.feature.nutrition.util.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class NutritionState(
    val todayMeals: List<MealRecord> = emptyList(),
    val recentMeals: List<MealRecord> = emptyList(),
    val dailyCalories: Float = 0f,
    val dailyProtein: Float = 0f,
    val dailyCarbs: Float = 0f,
    val dailyFat: Float = 0f,
    val dailyWaterIntake: Float = 0f,
    val waterIntakeGoal: Float = HealthConstants.DEFAULT_WATER_GOAL_ML,
    val mealCountToday: Int = 0,
    val weeklyCalories: List<Pair<String, Float>> = emptyList(),
    val cycleNutrition: CycleNutritionRecommendation? = null,
    val restingEnergy: Float = 0f,
    val exerciseCalories: Float = 0f,
    val totalExpenditure: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class AddMealState(
    val selectedDate: String = LocalDate.now().toString(),
    val selectedType: MealType = MealType.BREAKFAST,
    val foodName: String = "",
    val calories: String = "",
    val proteinGrams: String = "",
    val carbsGrams: String = "",
    val fatGrams: String = "",
    val servingSize: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

sealed class NutritionUiState {
    object Loading : NutritionUiState()
    data class Success(val state: NutritionState) : NutritionUiState()
    data class Error(val message: String) : NutritionUiState()
}

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val stringResolver: StringResolver,
    private val repository: NutritionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NutritionState())
    val state: StateFlow<NutritionState> = _state.asStateFlow()

    private val _addState = MutableStateFlow(AddMealState())
    val addState: StateFlow<AddMealState> = _addState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                repository.initialize()
                val today = LocalDate.now().toString()

                repository.getMealsByDate(today).collect { meals ->
                    _state.update {
                        it.copy(todayMeals = meals)
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }

        loadCycleNutrition()
        refreshDailyStats()
        refreshWeeklyStats()

        viewModelScope.launch {
            try {
                repository.getAllMeals().collect { meals ->
                    _state.update {
                        it.copy(recentMeals = meals.take(30))
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("NutritionViewModel", "Failed to collect meals", e)
            }
        }
    }

    fun updateSelectedDate(date: String) {
        _addState.update { it.copy(selectedDate = date) }
        viewModelScope.launch {
            try {
                repository.getMealsByDate(date).collect { meals ->
                    _state.update { it.copy(todayMeals = meals) }
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("NutritionViewModel", "Failed to collect meals by date", e)
            }
        }
        refreshDailyStats(date)
    }

    fun updateMealType(type: MealType) {
        _addState.update { it.copy(selectedType = type) }
    }

    fun updateFoodName(name: String) {
        _addState.update { it.copy(foodName = name) }
    }

    fun updateCalories(calories: String) {
        _addState.update { it.copy(calories = calories) }
    }

    fun updateProtein(protein: String) {
        _addState.update { it.copy(proteinGrams = protein) }
    }

    fun updateCarbs(carbs: String) {
        _addState.update { it.copy(carbsGrams = carbs) }
    }

    fun updateFat(fat: String) {
        _addState.update { it.copy(fatGrams = fat) }
    }

    fun updateServingSize(size: String) {
        _addState.update { it.copy(servingSize = size) }
    }

    fun updateNotes(notes: String) {
        _addState.update { it.copy(notes = notes) }
    }

    fun saveMeal() {
        val addState = _addState.value
        if (addState.foodName.isBlank()) {
            _addState.update { it.copy(error = stringResolver.getString(R.string.nutrition_error_food_name)) }
            return
        }

        viewModelScope.launch {
            _addState.update { it.copy(isSaving = true, error = null) }
            try {
                repository.addMeal(
                    mealType = addState.selectedType.value,
                    foodName = addState.foodName,
                    calories = addState.calories.toFloatOrNull(),
                    proteinGrams = addState.proteinGrams.toFloatOrNull(),
                    carbsGrams = addState.carbsGrams.toFloatOrNull(),
                    fatGrams = addState.fatGrams.toFloatOrNull(),
                    servingSize = addState.servingSize.ifBlank { null },
                    notes = addState.notes.ifBlank { null },
                    date = addState.selectedDate
                )
                refreshDailyStats(addState.selectedDate)
                refreshWeeklyStats()
                _addState.update {
                    AddMealState(
                        selectedDate = it.selectedDate,
                        saveSuccess = true
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _addState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun resetAddState() {
        _addState.value = AddMealState(selectedDate = _addState.value.selectedDate)
    }

    fun deleteMeal(record: MealRecord) {
        viewModelScope.launch {
            try {
                repository.deleteMeal(record)
                refreshDailyStats(record.date)
                refreshWeeklyStats()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun addWater(ml: Float) {
        viewModelScope.launch {
            try {
                val newTotal = repository.addWater(ml)
                _state.update { it.copy(dailyWaterIntake = newTotal) }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("NutritionViewModel", "Failed to add water", e)
            }
        }
    }

    fun updateWaterGoal(goal: Float) {
        _state.update { it.copy(waterIntakeGoal = goal) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
        _addState.update { it.copy(error = null) }
    }

    private fun refreshDailyStats(date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            try {
                val calories = repository.getDailyCalories(date)
                val protein = repository.getDailyProtein(date)
                val carbs = repository.getDailyCarbs(date)
                val fat = repository.getDailyFat(date)
                val mealCount = repository.getMealCountToday()
                val waterIntake = repository.getDailyWaterIntake(date)
                val restingEnergy = repository.getRestingEnergy()
                val exerciseCalories = repository.getExerciseCalories(date)
                val totalExpenditure = restingEnergy + exerciseCalories
                _state.update {
                    it.copy(
                        dailyCalories = calories,
                        dailyProtein = protein,
                        dailyCarbs = carbs,
                        dailyFat = fat,
                        dailyWaterIntake = waterIntake,
                        mealCountToday = mealCount,
                        restingEnergy = restingEnergy,
                        exerciseCalories = exerciseCalories,
                        totalExpenditure = totalExpenditure,
                        isLoading = false
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun refreshWeeklyStats() {
        viewModelScope.launch {
            try {
                val weekly = repository.getWeeklyCalories()
                _state.update { it.copy(weeklyCalories = weekly) }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("NutritionViewModel", "Failed to refresh weekly stats", e)
            }
        }
    }

    private fun loadCycleNutrition() {
        viewModelScope.launch {
            try {
                val recommendation = repository.getCycleNutritionRecommendation()
                _state.update { it.copy(cycleNutrition = recommendation) }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("NutritionViewModel", "Failed to load cycle nutrition", e)
            }
        }
    }
}
