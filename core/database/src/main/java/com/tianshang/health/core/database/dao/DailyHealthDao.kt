package com.tianshang.health.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tianshang.health.core.database.entity.DailyHealth
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHealthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(health: DailyHealth): Long

    @Update
    suspend fun update(health: DailyHealth)

    @Delete
    suspend fun delete(health: DailyHealth)

    @Query("SELECT * FROM daily_health WHERE id = :healthId")
    suspend fun getById(healthId: Long): DailyHealth?

    @Query("SELECT * FROM daily_health WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getByDate(userId: Long, date: String): DailyHealth?

    @Query("SELECT * FROM daily_health WHERE userId = :userId AND date = :date LIMIT 1")
    fun getByDateFlow(userId: Long, date: String): Flow<DailyHealth?>

    @Query("SELECT * FROM daily_health WHERE userId = :userId ORDER BY date DESC")
    fun getByUserId(userId: Long): Flow<List<DailyHealth>>

    @Query("SELECT * FROM daily_health WHERE userId = :userId ORDER BY date DESC")
    suspend fun getByUserIdList(userId: Long): List<DailyHealth>

    @Query(
        "SELECT * FROM daily_health WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC"
    )
    suspend fun getByDateRange(userId: Long, startDate: String, endDate: String): List<DailyHealth>

    @Query("SELECT * FROM daily_health WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(userId: Long, limit: Int): List<DailyHealth>

    @Query("SELECT COUNT(*) FROM daily_health WHERE userId = :userId")
    suspend fun getCount(userId: Long): Int

    @Query("DELETE FROM daily_health WHERE id = :healthId")
    suspend fun deleteById(healthId: Long)

    @Query(
        "SELECT AVG(sleepHours) FROM daily_health WHERE userId = :userId AND sleepHours IS NOT NULL AND date >= :startDate AND date <= :endDate"
    )
    suspend fun getAverageSleepHours(userId: Long, startDate: String, endDate: String): Float?

    @Query(
        "SELECT AVG(steps) FROM daily_health WHERE userId = :userId AND steps IS NOT NULL AND date >= :startDate AND date <= :endDate"
    )
    suspend fun getAverageSteps(userId: Long, startDate: String, endDate: String): Float?

    @Query(
        "SELECT AVG(moodScore) FROM daily_health WHERE userId = :userId AND moodScore IS NOT NULL AND date >= :startDate AND date <= :endDate"
    )
    suspend fun getAverageMood(userId: Long, startDate: String, endDate: String): Float?

    @Query(
        "SELECT AVG(sleepQuality) FROM daily_health WHERE userId = :userId AND sleepQuality IS NOT NULL AND date >= :startDate AND date <= :endDate"
    )
    suspend fun getAverageSleepQuality(userId: Long, startDate: String, endDate: String): Float?

    @Query(
        "SELECT AVG(stressLevel) FROM daily_health WHERE userId = :userId AND stressLevel IS NOT NULL AND date >= :startDate AND date <= :endDate"
    )
    suspend fun getAverageStress(userId: Long, startDate: String, endDate: String): Float?

    @Query("SELECT * FROM daily_health WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getTodayData(userId: Long, date: String): DailyHealth?

    @Transaction
    suspend fun insertOrUpdateWaterIntake(userId: Long, date: String, ml: Float) {
        val existing = getByDate(userId, date)
        if (existing != null) {
            update(existing.copy(waterIntake = ml, updatedAt = System.currentTimeMillis()))
        } else {
            insert(DailyHealth(userId = userId, date = date, waterIntake = ml))
        }
    }

    @Transaction
    suspend fun addWaterAtomic(userId: Long, date: String, ml: Float): Float {
        val existing = getByDate(userId, date)
        val newTotal = (existing?.waterIntake ?: 0f) + ml
        if (existing != null) {
            update(existing.copy(waterIntake = newTotal, updatedAt = System.currentTimeMillis()))
        } else {
            insert(DailyHealth(userId = userId, date = date, waterIntake = newTotal))
        }
        return newTotal
    }

    @Transaction
    suspend fun insertOrUpdateSleep(
        userId: Long,
        date: String,
        sleepHours: Float?,
        deepSleepHours: Float?,
        sleepQuality: Int?,
        bedTime: String?,
        wakeTime: String?,
        sleepLatency: Int?,
        wakeCount: Int?
    ) {
        val existing = getByDate(userId, date)
        if (existing != null) {
            update(
                existing.copy(
                    sleepHours = sleepHours ?: existing.sleepHours,
                    deepSleepHours = deepSleepHours ?: existing.deepSleepHours,
                    sleepQuality = sleepQuality ?: existing.sleepQuality,
                    bedTime = bedTime ?: existing.bedTime,
                    wakeTime = wakeTime ?: existing.wakeTime,
                    sleepLatency = sleepLatency ?: existing.sleepLatency,
                    wakeCount = wakeCount ?: existing.wakeCount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            insert(
                DailyHealth(
                    userId = userId, date = date,
                    sleepHours = sleepHours, deepSleepHours = deepSleepHours, sleepQuality = sleepQuality,
                    bedTime = bedTime, wakeTime = wakeTime, sleepLatency = sleepLatency, wakeCount = wakeCount
                )
            )
        }
    }

    @Transaction
    suspend fun insertOrUpdateNutrition(
        userId: Long,
        date: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fat: Float
    ) {
        val existing = getByDate(userId, date)
        if (existing != null) {
            update(
                existing.copy(
                    caloriesIntake = if (calories > 0f) calories else existing.caloriesIntake,
                    proteinGrams = if (protein > 0f) protein else existing.proteinGrams,
                    carbsGrams = if (carbs > 0f) carbs else existing.carbsGrams,
                    fatGrams = if (fat > 0f) fat else existing.fatGrams,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else if (calories > 0f || protein > 0f || carbs > 0f || fat > 0f) {
            insert(
                DailyHealth(
                    userId = userId,
                    date = date,
                    caloriesIntake = if (calories > 0f) calories else null,
                    proteinGrams = if (protein > 0f) protein else null,
                    carbsGrams = if (carbs > 0f) carbs else null,
                    fatGrams = if (fat > 0f) fat else null
                )
            )
        }
    }
}
