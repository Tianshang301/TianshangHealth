package com.tianshang.health.feature.period.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BmiRecord(
    val date: String,
    val weightKg: Float,
    val bmi: Float,
    val category: BmiCategory
)

enum class BmiCategory(val label: String, val min: Float, val max: Float) {
    UNDERWEIGHT("Underweight", 0f, HealthConstants.BMI_UNDERWEIGHT_MAX),
    NORMAL("Normal", HealthConstants.BMI_UNDERWEIGHT_MAX, HealthConstants.BMI_NORMAL_MAX),
    OVERWEIGHT("Overweight", HealthConstants.BMI_NORMAL_MAX, HealthConstants.BMI_OVERWEIGHT_MAX),
    OBESE("Obese", HealthConstants.BMI_OVERWEIGHT_MAX, Float.MAX_VALUE)
}

sealed class BmiUiState {
    object Loading : BmiUiState()
    data class Success(
        val currentBmi: Float?,
        val category: BmiCategory?,
        val heightCm: Float?,
        val records: List<BmiRecord>
    ) : BmiUiState()
    data class Error(val message: String) : BmiUiState()
}

@HiltViewModel
class BmiViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dailyHealthDao: DailyHealthDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<BmiUiState>(BmiUiState.Loading)
    val uiState: StateFlow<BmiUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = BmiUiState.Loading
                currentUser = userRepository.getOrCreateDefault()
                val user = currentUser ?: throw IllegalStateException("No user")
                val healthRecords = dailyHealthDao.getByUserIdList(user.id)
                val weightRecords = healthRecords.filter { it.weightKg != null }.sortedByDescending { it.date }

                val bmiRecords = weightRecords.mapNotNull { health ->
                    val height = user.heightCm
                    val weight = health.weightKg
                    if (height != null && height > 0f && weight != null) {
                        val bmi = weight / ((height / HealthConstants.CM_PER_METER) * (height / HealthConstants.CM_PER_METER))
                        BmiRecord(
                            date = health.date,
                            weightKg = weight,
                            bmi = bmi,
                            category = categorizeBmi(bmi)
                        )
                    } else {
                        null
                    }
                }

                val latest = bmiRecords.firstOrNull()

                _uiState.value = BmiUiState.Success(
                    currentBmi = latest?.bmi,
                    category = latest?.category,
                    heightCm = user.heightCm,
                    records = bmiRecords
                )
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = BmiUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveHeight(heightCm: Float) {
        viewModelScope.launch {
            currentUser?.let { user ->
                userRepository.updateHeight(user.id, heightCm)
                loadData()
            }
        }
    }

    fun addWeightRecord(weightKg: Float, date: LocalDate) {
        viewModelScope.launch {
            currentUser?.let { user ->
                val existing = dailyHealthDao.getByDate(user.id, date.toString())
                if (existing != null) {
                    dailyHealthDao.update(existing.copy(weightKg = weightKg))
                } else {
                    dailyHealthDao.insert(
                        DailyHealth(
                            userId = user.id,
                            date = date.toString(),
                            weightKg = weightKg
                        )
                    )
                }
                loadData()
            }
        }
    }

    fun refresh() {
        loadData()
    }

    companion object {
        fun categorizeBmi(bmi: Float): BmiCategory {
            return BmiCategory.entries.firstOrNull { bmi >= it.min && bmi < it.max } ?: BmiCategory.NORMAL
        }
    }
}
