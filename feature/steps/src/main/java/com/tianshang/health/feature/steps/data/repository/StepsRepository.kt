package com.tianshang.health.feature.steps.data.repository

import com.tianshang.health.core.common.util.ValidationUtils
import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.core.database.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepsRepository @Inject constructor(
    private val stepsDao: StepsDao,
    private val userRepository: UserRepository
) {

    private var currentUserId: Long = 0

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps.asStateFlow()

    suspend fun initialize() {
        val user = userRepository.getOrCreateDefault()
        currentUserId = user.id
        val today = LocalDate.now().toString()
        val existing = stepsDao.getByDate(currentUserId, today)
        _todaySteps.value = existing?.count ?: 0
    }

    fun observeTodaySteps(): StateFlow<Int> = todaySteps

    fun getCurrentTodaySteps(): Int = _todaySteps.value

    fun getTodaySteps(): Flow<DailySteps?> {
        val today = LocalDate.now().toString()
        return stepsDao.getByDateFlow(currentUserId, today)
    }

    suspend fun getTodayStepsSync(): DailySteps? {
        val today = LocalDate.now().toString()
        return stepsDao.getByDate(currentUserId, today)
    }

    fun getRecentSteps(days: Int = 7): Flow<List<DailySteps>> {
        require(ValidationUtils.isValidLimit(days)) { "Invalid days limit: $days" }
        return stepsDao.getRecentFlow(currentUserId, days)
    }

    fun getAllSteps(): Flow<List<DailySteps>> {
        return stepsDao.getByUserId(currentUserId)
    }

    suspend fun getRecentStepsSync(days: Int = 7): List<DailySteps> {
        require(ValidationUtils.isValidLimit(days)) { "Invalid days limit: $days" }
        return stepsDao.getRecent(currentUserId, days)
    }

    fun getStepsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailySteps>> {
        require(ValidationUtils.isValidDate(startDate)) { "startDate cannot be in the future: $startDate" }
        require(
            ValidationUtils.isValidDateRange(startDate, endDate)
        ) { "endDate before startDate: $endDate < $startDate" }
        return stepsDao.getByDateRangeFlow(currentUserId, startDate.toString(), endDate.toString())
    }

    suspend fun getStepsByDateRangeSync(startDate: LocalDate, endDate: LocalDate): List<DailySteps> {
        require(ValidationUtils.isValidDate(startDate)) { "startDate cannot be in the future: $startDate" }
        require(
            ValidationUtils.isValidDateRange(startDate, endDate)
        ) { "endDate before startDate: $endDate < $startDate" }
        return stepsDao.getByDateRange(currentUserId, startDate.toString(), endDate.toString())
    }

    suspend fun addSteps(count: Int) {
        require(ValidationUtils.isValidStepCount(count)) { "Invalid step count: $count" }
        _todaySteps.value += count
        val today = LocalDate.now().toString()
        stepsDao.insertOrAddSteps(currentUserId, today, count)
    }

    suspend fun updateGoal(goal: Int) {
        require(ValidationUtils.isValidNonNegativeInt(goal)) { "Invalid goal: $goal" }
        val today = LocalDate.now().toString()
        val existing = stepsDao.getByDate(currentUserId, today)

        if (existing != null) {
            stepsDao.update(existing.copy(goal = goal, updatedAt = System.currentTimeMillis()))
        } else {
            val newEntry = DailySteps(
                userId = currentUserId,
                date = today,
                count = 0,
                goal = goal
            )
            stepsDao.insert(newEntry)
        }
    }

    suspend fun getTotalSteps(startDate: LocalDate, endDate: LocalDate): Int {
        return stepsDao.getTotalSteps(currentUserId, startDate.toString(), endDate.toString()) ?: 0
    }

    suspend fun getAverageSteps(startDate: LocalDate, endDate: LocalDate): Float {
        return stepsDao.getAverageSteps(currentUserId, startDate.toString(), endDate.toString()) ?: 0f
    }

    suspend fun getTotalStepsAllTime(): Int {
        return stepsDao.getTotalStepsAllTime(currentUserId) ?: 0
    }

    suspend fun markSyncedToHealthConnect(stepsId: Long) {
        require(ValidationUtils.isValidId(stepsId)) { "Invalid stepsId: $stepsId" }
        stepsDao.markSyncedToHealthConnect(stepsId)
    }
}
