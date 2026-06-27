package com.tianshang.health.feature.analysis.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.feature.analysis.domain.report.MedicalReportGenerator
import com.tianshang.health.feature.analysis.util.LocalizedContextProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stringResolver: StringResolver,
    private val userRepository: UserRepository,
    private val periodRecordDao: PeriodRecordDao,
    private val dailyHealthDao: DailyHealthDao
) : ViewModel() {

    enum class ReportSection {
        PERIOD, ACTIVITY, SLEEP, NUTRITION
    }

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState

    private val _isFemale = MutableStateFlow(true)
    val isFemale: StateFlow<Boolean> = _isFemale

    private val _selectedSections = MutableStateFlow(
        setOf(
            ReportSection.PERIOD,
            ReportSection.ACTIVITY,
            ReportSection.SLEEP,
            ReportSection.NUTRITION
        )
    )
    val selectedSections: StateFlow<Set<ReportSection>> = _selectedSections

    val availableSections: List<ReportSection> get() = if (_isFemale.value) {
        ReportSection.entries
    } else {
        ReportSection.entries - ReportSection.PERIOD
    }

    init {
        viewModelScope.launch {
            val user = userRepository.getOrCreateDefault()
            val female = User.Gender.fromValue(user.gender) != User.Gender.MALE
            _isFemale.value = female
            if (!female) {
                _selectedSections.value = _selectedSections.value - ReportSection.PERIOD
            }
        }
    }

    fun toggleSection(section: ReportSection) {
        _selectedSections.value = _selectedSections.value.let { current ->
            if (section in current) current - section else current + section
        }
    }

    fun generateReport(days: Int = HealthConstants.REPORT_DEFAULT_DAYS) {
        viewModelScope.launch {
            _uiState.value = ReportUiState.Generating
            try {
                val user = userRepository.getOrCreateDefault()
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(days.toLong())
                val sections = _selectedSections.value

                val periodRecords = if (ReportSection.PERIOD in sections) {
                    periodRecordDao.getByUserIdList(user.id)
                        .filter { !it.isDeleted }
                        .filter {
                            val recordDate = LocalDate.parse(it.startDate)
                            recordDate >= startDate && recordDate <= endDate
                        }
                } else {
                    emptyList()
                }

                val healthData = if (sections.any { it != ReportSection.PERIOD }) {
                    dailyHealthDao.getByDateRange(
                        user.id,
                        startDate.toString(),
                        endDate.toString()
                    )
                } else {
                    emptyList()
                }

                val localizedContext = LocalizedContextProvider.getLocalizedContext(context)
                val generator = MedicalReportGenerator(localizedContext)
                val reportData = MedicalReportGenerator.HealthReportData(
                    userName = user.name,
                    gender = user.gender,
                    reportPeriod = "$startDate ~ $endDate",
                    periodRecords = periodRecords,
                    dailyHealthData = healthData,
                    includePeriod = ReportSection.PERIOD in sections,
                    includeActivity = ReportSection.ACTIVITY in sections,
                    includeSleep = ReportSection.SLEEP in sections,
                    includeNutrition = ReportSection.NUTRITION in sections
                )

                val uri = generator.generateReport(reportData)
                if (uri != null) {
                    _uiState.value = ReportUiState.Success(uri)
                } else {
                    _uiState.value = ReportUiState.Error(stringResolver.getString(R.string.error_failed_generate_report))
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = ReportUiState.Error(e.message ?: stringResolver.getString(R.string.error_unknown))
            }
        }
    }

    fun resetState() {
        _uiState.value = ReportUiState.Idle
    }
}

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Generating : ReportUiState()
    data class Success(val uri: android.net.Uri) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}
