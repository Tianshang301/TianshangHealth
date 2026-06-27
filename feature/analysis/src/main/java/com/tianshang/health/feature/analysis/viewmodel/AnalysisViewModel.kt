package com.tianshang.health.feature.analysis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.feature.analysis.domain.AnalysisData
import com.tianshang.health.feature.analysis.domain.AnalysisUiState
import com.tianshang.health.feature.analysis.domain.AnalyticsEngine
import com.tianshang.health.feature.analysis.domain.SuggestionEngine
import com.tianshang.health.feature.analysis.domain.TrendAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val stringResolver: StringResolver,
    private val dailyHealthDao: DailyHealthDao,
    private val periodRecordDao: PeriodRecordDao,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Loading)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
        _uiState.value = AnalysisUiState.Loading
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val user = userDao.getFirst()
                if (user == null) {
                    _uiState.value = AnalysisUiState.Error(stringResolver.getString(R.string.error_onboarding_required))
                    return@launch
                }
                val currentUserId = user.id
                val isFemale = User.Gender.fromValue(user.gender) != User.Gender.MALE

                val today = LocalDate.now()
                val dateRange = (0..27).map { today.minusDays(it.toLong()) }
                val startDate = dateRange.last().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

                val records = dailyHealthDao.getByDateRange(currentUserId, startDate, endDate)
                val last7 = if (records.size > 7) records.takeLast(7) else records
                val last14 = if (records.size > 14) records.takeLast(14) else records
                val previous7 = if (records.size > 14) records.dropLast(7).takeLast(7) else emptyList()
                val fullHistory = records

                val nutrition = AnalyticsEngine.computeNutrition(last7)
                val sleep = AnalyticsEngine.computeSleep(last7)
                val exercise = AnalyticsEngine.computeExercise(last7)
                val calorieBalance = AnalyticsEngine.computeCalorieBalance(last7, user)

                var phaseComparisons = emptyList<com.tianshang.health.feature.analysis.domain.PhaseComparison>()
                if (isFemale) {
                    val periodRecords = periodRecordDao.getByUserIdList(currentUserId)
                    if (periodRecords.size >= 2) {
                        val sorted = periodRecords.sortedByDescending { it.startDate }
                        val lastPeriodStart = LocalDate.parse(sorted[0].startDate)
                        val prevPeriodStart = LocalDate.parse(sorted[1].startDate)
                        val cycleLen = java.time.temporal.ChronoUnit.DAYS.between(
                            prevPeriodStart,
                            lastPeriodStart
                        ).toInt()
                        val ovulationDay = lastPeriodStart.plusDays((cycleLen - 14).toLong())
                        val follicularEnd = ovulationDay.minusDays(1)
                        val lutealStart = ovulationDay.plusDays(1)
                        val lutealEnd = lastPeriodStart.plusDays(cycleLen.toLong()).minusDays(1)

                        phaseComparisons = AnalyticsEngine.computePhaseComparisons(
                            records = last14,
                            follicularDateRange = Pair(lastPeriodStart.format(DateTimeFormatter.ISO_LOCAL_DATE), follicularEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                            lutealDateRange = Pair(lutealStart.format(DateTimeFormatter.ISO_LOCAL_DATE), lutealEnd.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        )
                    }
                }

                val crossDimensionReport = TrendAnalyzer.computeCrossDimensionReport(
                    currentRecords = last14,
                    previousRecords = previous7,
                    fullHistory = fullHistory
                )

                val analysisData = AnalysisData(
                    nutrition = if (nutrition.avgCalories > 0) nutrition else null,
                    sleep = if (sleep.avgHours > 0) sleep else null,
                    exercise = if (exercise.totalMinutes > 0) exercise else null,
                    calorieBalance = if (calorieBalance.avgCaloriesIn > 0 || calorieBalance.avgCaloriesBurned > 0) calorieBalance else null,
                    phaseComparisons = phaseComparisons,
                    suggestions = emptyList(),
                    crossDimensionReport = crossDimensionReport
                )

                val suggestions = SuggestionEngine.generate(analysisData)

                _uiState.value = AnalysisUiState.Success(
                    data = analysisData.copy(suggestions = suggestions),
                    isFemale = isFemale
                )
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = AnalysisUiState.Error(e.message ?: stringResolver.getString(R.string.error_load_failed))
            }
        }
    }
}
