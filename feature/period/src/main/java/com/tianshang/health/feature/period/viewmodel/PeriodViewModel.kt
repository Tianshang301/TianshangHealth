package com.tianshang.health.feature.period.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.PeriodRecordRepository
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.core.period.api.PredictionResult
import com.tianshang.health.feature.period.engine.PredictionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PeriodState(
    val records: List<PeriodRecord> = emptyList(),
    val prediction: PredictionResult? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val isRecording: Boolean = false,
    val currentCycleDay: Int? = null,
    val daysUntilNextPeriod: Int? = null
)

sealed class PeriodUiState {
    object Loading : PeriodUiState()
    data class Success(val periodState: PeriodState) : PeriodUiState()
    data class Error(val message: String) : PeriodUiState()
}

@HiltViewModel
class PeriodViewModel @Inject constructor(
    private val stringResolver: StringResolver,
    private val periodRecordRepository: PeriodRecordRepository,
    private val userRepository: UserRepository,
    private val predictionEngine: PredictionEngine,
    private val stepsDao: StepsDao,
    private val dailyHealthDao: DailyHealthDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<PeriodUiState>(PeriodUiState.Loading)
    val uiState: StateFlow<PeriodUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _userFlow = userRepository.getAll()
        .map { users -> users.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userGender: StateFlow<User.Gender> = _userFlow
        .map { user -> user?.let { User.Gender.fromValue(it.gender) } ?: User.Gender.OTHER }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), User.Gender.OTHER)

    val todaySteps: StateFlow<DailySteps?> = _userFlow
        .flatMapLatest { user ->
            if (user != null && user.id > 0L) {
                stepsDao.getByDateFlow(user.id, LocalDate.now().toString())
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentSteps: StateFlow<List<DailySteps>> = _userFlow
        .flatMapLatest { user ->
            if (user != null && user.id > 0L) {
                stepsDao.getRecentFlow(user.id, HealthConstants.RECENT_DAYS_WEEK)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySteps: StateFlow<List<DailySteps>> = _userFlow
        .flatMapLatest { user ->
            if (user != null && user.id > 0L) {
                stepsDao.getRecentFlow(user.id, HealthConstants.RECENT_DAYS_MONTH)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val yearlySteps: StateFlow<List<DailySteps>> = _userFlow
        .flatMapLatest { user ->
            if (user != null && user.id > 0L) {
                stepsDao.getByDateRangeFlow(
                    user.id,
                    LocalDate.now().minusDays(HealthConstants.DAYS_IN_YEAR).toString(),
                    LocalDate.now().toString()
                )
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSteps: StateFlow<List<DailySteps>> = _userFlow
        .flatMapLatest { user ->
            if (user != null && user.id > 0L) {
                stepsDao.getByUserId(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userWeight = MutableStateFlow<Float?>(null)
    val userWeight: StateFlow<Float?> = _userWeight.asStateFlow()

    private val _userHeight = MutableStateFlow<Float?>(null)
    val userHeight: StateFlow<Float?> = _userHeight.asStateFlow()

    private val _isMale = MutableStateFlow(false)
    val isMale: StateFlow<Boolean> = _isMale.asStateFlow()

    private var currentUserId: Long = 0

    init {
        loadData()
        loadBodyMetrics()
    }

    private fun loadBodyMetrics() {
        viewModelScope.launch {
            try {
                val user = userRepository.getOrCreateDefault()
                _userHeight.value = user.heightCm
                _isMale.value = user.gender == "male"

                val recent = dailyHealthDao.getRecent(user.id, HealthConstants.RECENT_DAYS_MONTH)
                _userWeight.value = recent.firstOrNull { it.weightKg != null }?.weightKg
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                Log.e("PeriodViewModel", "Failed to load body metrics", e)
            }
        }
    }

    fun stepsToKcal(steps: Int): Float {
        val weight = _userWeight.value ?: HealthConstants.DEFAULT_WEIGHT_KG
        val height = _userHeight.value
        if (height != null && height in HealthConstants.MIN_HEIGHT_CM..HealthConstants.MAX_HEIGHT_CM) {
            val strideLengthKm = height * HealthConstants.STRIDE_LENGTH_COEFFICIENT / 100_000f
            val distanceKm = steps * strideLengthKm
            return distanceKm * weight * HealthConstants.WALKING_KCAL_PER_KG_KM
        }
        return steps * HealthConstants.STEP_TO_KCAL_BASE_FACTOR * (weight / HealthConstants.DEFAULT_WEIGHT_KG)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = PeriodUiState.Loading

                val user = userRepository.getOrCreateDefault()
                currentUserId = user.id

                val records = periodRecordRepository.getByUserIdList(currentUserId)
                val prediction = predictionEngine.predict(records)

                val cycleDay = calculateCurrentCycleDay(records)
                val daysUntil = calculateDaysUntilNextPeriod(prediction)

                val periodState = PeriodState(
                    records = records,
                    prediction = prediction,
                    selectedDate = _selectedDate.value,
                    currentCycleDay = cycleDay,
                    daysUntilNextPeriod = daysUntil
                )

                _uiState.value = PeriodUiState.Success(periodState)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = PeriodUiState.Error(stringResolver.getString(R.string.error_unknown))
            }
        }
    }

    private fun calculateCurrentCycleDay(records: List<PeriodRecord>): Int? {
        if (records.isEmpty()) return null

        val sortedRecords = records.sortedByDescending { it.startDate }
        val lastRecord = sortedRecords.first()
        val lastStartDate = LocalDate.parse(lastRecord.startDate)
        val today = LocalDate.now()

        val daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(lastStartDate, today).toInt()

        return if (daysSinceStart >= 0) daysSinceStart + 1 else null
    }

    private fun calculateDaysUntilNextPeriod(prediction: PredictionResult?): Int? {
        if (prediction == null) return null

        val today = LocalDate.now()
        val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, prediction.nextPeriodStart).toInt()

        return if (daysUntil >= 0) daysUntil else null
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addPeriodRecord(
        startDate: LocalDate,
        endDate: LocalDate?,
        flowLevel: Int?,
        painLevel: Int?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val record = PeriodRecord(
                    userId = currentUserId,
                    startDate = startDate.toString(),
                    endDate = endDate?.toString(),
                    flowLevel = flowLevel,
                    painLevel = painLevel,
                    notes = notes
                )

                periodRecordRepository.insert(record)
                loadData()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = PeriodUiState.Error(stringResolver.getString(R.string.error_failed_add_record))
            }
        }
    }

    fun updatePeriodRecord(record: PeriodRecord) {
        viewModelScope.launch {
            try {
                periodRecordRepository.update(record)
                loadData()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = PeriodUiState.Error(stringResolver.getString(R.string.error_failed_update_record))
            }
        }
    }

    fun deletePeriodRecord(recordId: Long) {
        viewModelScope.launch {
            try {
                periodRecordRepository.deleteById(recordId)
                loadData()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = PeriodUiState.Error(stringResolver.getString(R.string.error_failed_delete_record))
            }
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val activeRecord = periodRecordRepository.getActiveRecordOnDate(currentUserId, today.toString())

                if (activeRecord != null) {
                    // Already recording, end the period
                    val updatedRecord = activeRecord.copy(
                        endDate = today.toString(),
                        updatedAt = System.currentTimeMillis()
                    )
                    periodRecordRepository.update(updatedRecord)
                } else {
                    // Start new period
                    val newRecord = PeriodRecord(
                        userId = currentUserId,
                        startDate = today.toString()
                    )
                    periodRecordRepository.insert(newRecord)
                }

                loadData()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = PeriodUiState.Error(stringResolver.getString(R.string.error_failed_record))
            }
        }
    }

    fun isPeriodActive(): Boolean {
        val state = _uiState.value
        if (state is PeriodUiState.Success) {
            val today = LocalDate.now()
            return state.periodState.records.any { record ->
                val start = LocalDate.parse(record.startDate)
                val end = record.endDate?.let { LocalDate.parse(it) }
                !today.isBefore(start) && (end == null || !today.isAfter(end))
            }
        }
        return false
    }

    fun refresh() {
        loadData()
    }

    fun getPredictionExplanation(): String {
        val state = _uiState.value
        if (state is PeriodUiState.Success) {
            return state.periodState.prediction?.explanation ?: stringResolver.getString(R.string.prediction_not_enough_data)
        }
        return stringResolver.getString(R.string.loading)
    }

    fun getCycleRegularity(): String {
        val state = _uiState.value
        if (state is PeriodUiState.Success) {
            val records = state.periodState.records
            if (records.size < 3) return stringResolver.getString(R.string.insufficient_data)

            val cycleLengths = mutableListOf<Int>()
            val sortedRecords = records.sortedBy { it.startDate }

            for (i in 1 until sortedRecords.size) {
                val prev = LocalDate.parse(sortedRecords[i - 1].startDate)
                val curr = LocalDate.parse(sortedRecords[i].startDate)
                val days = java.time.temporal.ChronoUnit.DAYS.between(prev, curr).toInt()
                if (days in HealthConstants.MIN_CYCLE_LENGTH..HealthConstants.MAX_CYCLE_LENGTH) {
                    cycleLengths.add(days)
                }
            }

            return predictionEngine.calculateCycleRegularity(cycleLengths)
        }
        return stringResolver.getString(R.string.loading)
    }
}
