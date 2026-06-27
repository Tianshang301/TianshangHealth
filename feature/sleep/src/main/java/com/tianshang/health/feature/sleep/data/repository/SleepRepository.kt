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

    suspend fun getDateRange(startDate: String, endDate: String): List<DailyHealth> {
        val userId = requireUserId()
        return dailyHealthDao.getByDateRange(userId, startDate, endDate)
    }

    suspend fun saveSleep(
        date: String,
        sleepHours: Float?,
        deepSleepHours: Float?,
        sleepQuality: Int?,
        bedTime: String? = null,
        wakeTime: String? = null,
        sleepLatency: Int? = null,
        wakeCount: Int? = null
    ) {
        require(ValidationUtils.isValidDateString(date)) { "Invalid date: $date" }
        require(ValidationUtils.isValidNonNegative(sleepHours)) { "Invalid sleepHours: $sleepHours" }
        require(ValidationUtils.isValidNonNegative(deepSleepHours)) { "Invalid deepSleepHours: $deepSleepHours" }
        require(ValidationUtils.isValidSleepQuality(sleepQuality)) { "Invalid sleepQuality: $sleepQuality" }
        val userId = requireUserId()
        dailyHealthDao.insertOrUpdateSleep(
            userId, date,
            sleepHours, deepSleepHours, sleepQuality,
            bedTime, wakeTime, sleepLatency, wakeCount
        )
    }

    data class SleepConsistencyScore(
        val bedCV: Float?,
        val wakeCV: Float?,
        val durationCV: Float?,
        val overallScore: Float
    )

    suspend fun getSleepConsistency(days: Int = 14): SleepConsistencyScore? {
        val records = getRecentDays(days).filter {
            it.bedTime != null || it.wakeTime != null
        }
        if (records.size < 3) return null

        fun minutesSinceMidnight(time: String?): Float? {
            if (time == null) return null
            val parts = time.split(":")
            if (parts.size != 2) return null
            return parts[0].toFloatOrNull()?.let { h ->
                parts[1].toFloatOrNull()?.let { m -> h * 60 + m }
            }
        }

        val bedMinutes = records.mapNotNull { minutesSinceMidnight(it.bedTime) }
        val wakeMinutes = records.mapNotNull { minutesSinceMidnight(it.wakeTime) }
        val durations = records.mapNotNull { it.sleepHours }

        fun coefficientOfVariation(values: List<Float>): Float? {
            if (values.size < 3) return null
            val mean = values.average().toFloat()
            if (mean == 0f) return null
            val variance = values.map { (it - mean) * (it - mean) }.average().toFloat()
            return kotlin.math.sqrt(variance) / mean
        }

        val bedCV = coefficientOfVariation(bedMinutes)
        val wakeCV = coefficientOfVariation(wakeMinutes)
        val durationCV = coefficientOfVariation(durations)

        val bedScore = bedCV?.let { (1f - it.coerceIn(0f, 1f)) * 100f } ?: 50f
        val wakeScore = wakeCV?.let { (1f - it.coerceIn(0f, 1f)) * 100f } ?: 50f
        val durationScore = durationCV?.let { (1f - it.coerceIn(0f, 1f)) * 100f } ?: 50f
        val overall = bedScore * 0.4f + wakeScore * 0.4f + durationScore * 0.2f

        return SleepConsistencyScore(
            bedCV = bedCV,
            wakeCV = wakeCV,
            durationCV = durationCV,
            overallScore = overall.coerceIn(0f, 100f)
        )
    }
}
