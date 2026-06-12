package com.tianshang.health.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}
