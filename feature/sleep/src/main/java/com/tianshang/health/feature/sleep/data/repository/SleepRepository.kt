package com.tianshang.health.feature.sleep.data.repository

import com.tianshang.health.core.common.util.ValidationUtils
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.entity.DailyHealth
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    private val dailyHealthDao: DailyHealthDao,
    private val userDao: UserDao
) {
    private var currentUserId: Long? = null

    suspend fun initialize() {
        if (currentUserId == null) {
            currentUserId = userDao.getFirst()?.id
        }
    }

    private suspend fun requireUserId(): Long {
        val userId = currentUserId ?: userDao.getFirst()?.id
        currentUserId = userId
        return userId ?: throw IllegalStateException("No user found. Onboarding must be completed first.")
    }

    suspend fun getTodaySleep(date: String = LocalDate.now().toString()): DailyHealth? {
        val userId = requireUserId()
        return dailyHealthDao.getByDate(userId, date)
    }

    suspend fun getRecentDays(count: Int = 7): List<DailyHealth> {
        require(ValidationUtils.isValidLimit(count)) { "Invalid count: $count" }
        val userId = requireUserId()
        val today = LocalDate.now()
        val startDate = today.minusDays(count.toLong() - 1)
        return dailyHealthDao.getByDateRange(userId, startDate.toString(), today.toString())
    }

    suspend fun saveSleep(
        date: String,
        sleepHours: Float?,
        deepSleepHours: Float?,
        sleepQuality: Int?
    ) {
        require(ValidationUtils.isValidDateString(date)) { "Invalid date: $date" }
        require(ValidationUtils.isValidNonNegative(sleepHours)) { "Invalid sleepHours: $sleepHours" }
        require(ValidationUtils.isValidNonNegative(deepSleepHours)) { "Invalid deepSleepHours: $deepSleepHours" }
        require(ValidationUtils.isValidSleepQuality(sleepQuality)) { "Invalid sleepQuality: $sleepQuality" }
        val userId = requireUserId()
        val existing = dailyHealthDao.getByDate(userId, date)
        if (existing != null) {
            dailyHealthDao.update(
                existing.copy(
                    sleepHours = sleepHours ?: existing.sleepHours,
                    deepSleepHours = deepSleepHours ?: existing.deepSleepHours,
                    sleepQuality = sleepQuality ?: existing.sleepQuality,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            dailyHealthDao.insert(
                DailyHealth(
                    userId = userId,
                    date = date,
                    sleepHours = sleepHours,
                    deepSleepHours = deepSleepHours,
                    sleepQuality = sleepQuality
                )
            )
        }
    }
}
