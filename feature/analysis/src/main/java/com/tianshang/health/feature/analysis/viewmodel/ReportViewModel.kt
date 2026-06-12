package com.tianshang.health.feature.analysis.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
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

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState

    fun generateReport(days: Int = HealthConstants.REPORT_DEFAULT_DAYS) {
        viewModelScope.launch {
            _uiState.value = ReportUiState.Generating
            try {
                val user = userRepository.getOrCreateDefault()
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(days.toLong())

                val periodRecords = periodRecordDao.getByUserIdList(user.id)
                    .filter { !it.isDeleted }
                    .filter {
                        val recordDate = LocalDate.parse(it.startDate)
                        recordDate >= startDate && recordDate <= endDate
                    }

                val healthData = dailyHealthDao.getByDateRange(
                    user.id,
                    startDate.toString(),
                    endDate.toString()
                )

                val localizedContext = LocalizedContextProvider.getLocalizedContext(context)
                val generator = MedicalReportGenerator(localizedContext)
                val reportData = MedicalReportGenerator.HealthReportData(
                    userName = user.name,
                    gender = user.gender,
                    reportPeriod = "$startDate ~ $endDate",
                    periodRecords = periodRecords,
                    dailyHealthData = healthData
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
