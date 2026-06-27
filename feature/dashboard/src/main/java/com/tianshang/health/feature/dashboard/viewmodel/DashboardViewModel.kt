package com.tianshang.health.feature.dashboard.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.database.repository.PeriodRecordRepository
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.feature.dashboard.domain.GetHealthInsightsUseCase
import com.tianshang.health.feature.dashboard.domain.InsightResult
import com.tianshang.health.feature.onboarding.model.Gender
import com.tianshang.health.feature.steps.data.repository.StepsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@Stable
data class DashboardState(
    val userGender: Gender = Gender.FEMALE,
    val todaySteps: Int = 0,
    val stepsGoal: Int = HealthConstants.DEFAULT_STEPS_GOAL,
    val weeklySteps: List<DailySteps> = emptyList(),
    val recentPeriodRecords: List<PeriodRecord> = emptyList(),
    val isPeriodActive: Boolean = false,
    val currentCycleDay: Int? = null,
    val insights: InsightResult = InsightResult.EMPTY,
    val isLoading: Boolean = true
)

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val state: DashboardState) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val stringResolver: StringResolver,
    private val userRepository: UserRepository,
    private val stepsRepository: StepsRepository,
    private val periodRecordRepository: PeriodRecordRepository,
    private val getHealthInsights: GetHealthInsightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeDashboard()
    }

    private fun observeDashboard() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            try {
                stepsRepository.initialize()
                userRepository.getOrCreateDefault()

                val today = LocalDate.now()
                val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

                combine(
                    userRepository.getAll(),
                    stepsRepository.getTodaySteps(),
                    stepsRepository.getStepsByDateRange(weekStart, today)
                ) { users, todaySteps, weeklySteps ->
                    val user = if (users.isNotEmpty()) users.first() else userRepository.getOrCreateDefault()
                    val userGender = Gender.fromValue(user.gender)

                    val recentPeriodRecords = if (userGender != Gender.MALE) {
                        periodRecordRepository.getRecentRecords(user.id, 5)
                    } else {
                        emptyList()
                    }

                    val isPeriodActive = if (userGender != Gender.MALE) {
                        periodRecordRepository.getActiveRecordOnDate(user.id, today.toString()) != null
                    } else {
                        false
                    }

                    val currentCycleDay = if (userGender != Gender.MALE && recentPeriodRecords.isNotEmpty()) {
                        val lastRecord = recentPeriodRecords.first()
                        val lastStartDate = LocalDate.parse(lastRecord.startDate)
                        java.time.temporal.ChronoUnit.DAYS.between(lastStartDate, today).toInt() + 1
                    } else {
                        null
                    }

                    val insights = getHealthInsights(userGender)

                    DashboardState(
                        userGender = userGender,
                        todaySteps = todaySteps?.count ?: 0,
                        stepsGoal = todaySteps?.goal ?: HealthConstants.DEFAULT_STEPS_GOAL,
                        weeklySteps = weeklySteps,
                        recentPeriodRecords = recentPeriodRecords,
                        isPeriodActive = isPeriodActive,
                        currentCycleDay = currentCycleDay,
                        insights = insights,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = DashboardUiState.Success(state)
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(stringResolver.getString(R.string.error_unknown))
            }
        }
    }

    fun refresh() {
        observeDashboard()
    }
}
