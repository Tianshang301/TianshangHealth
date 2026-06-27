package com.tianshang.health.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tianshang.health.core.database.entity.DailySteps
import kotlinx.coroutines.flow.Flow

@Dao
interface StepsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(steps: DailySteps): Long

    @Update
    suspend fun update(steps: DailySteps)

    @Delete
    suspend fun delete(steps: DailySteps)

    @Query("SELECT * FROM daily_steps WHERE id = :stepsId")
    suspend fun getById(stepsId: Long): DailySteps?

    @Query("SELECT * FROM daily_steps WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getByDate(userId: Long, date: String): DailySteps?

    @Query("SELECT * FROM daily_steps WHERE userId = :userId AND date = :date LIMIT 1")
    fun getByDateFlow(userId: Long, date: String): Flow<DailySteps?>

    @Query("SELECT * FROM daily_steps WHERE userId = :userId ORDER BY date DESC")
    fun getByUserId(userId: Long): Flow<List<DailySteps>>

    @Query(
        "SELECT * FROM daily_steps WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC"
    )
    suspend fun getByDateRange(userId: Long, startDate: String, endDate: String): List<DailySteps>

    @Query(
        "SELECT * FROM daily_steps WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC"
    )
    fun getByDateRangeFlow(userId: Long, startDate: String, endDate: String): Flow<List<DailySteps>>

    @Query("SELECT * FROM daily_steps WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(userId: Long, limit: Int): List<DailySteps>

    @Query("SELECT * FROM daily_steps WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentFlow(userId: Long, limit: Int): Flow<List<DailySteps>>

    @Query("SELECT COUNT(*) FROM daily_steps WHERE userId = :userId")
    suspend fun getCount(userId: Long): Int

    @Query("SELECT SUM(count) FROM daily_steps WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalSteps(userId: Long, startDate: String, endDate: String): Int?

    @Query("SELECT SUM(count) FROM daily_steps WHERE userId = :userId")
    suspend fun getTotalStepsAllTime(userId: Long): Int?

    @Query("SELECT AVG(count) FROM daily_steps WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    suspend fun getAverageSteps(userId: Long, startDate: String, endDate: String): Float?

    @Query(
        "UPDATE daily_steps SET count = count + :additionalSteps, updatedAt = :updatedAt WHERE userId = :userId AND date = :date"
    )
    suspend fun addSteps(userId: Long, date: String, additionalSteps: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE daily_steps SET syncedToHealthConnect = 1 WHERE id = :stepsId")
    suspend fun markSyncedToHealthConnect(stepsId: Long)

    @Query("DELETE FROM daily_steps WHERE id = :stepsId")
    suspend fun deleteById(stepsId: Long)

    @Transaction
    suspend fun insertOrAddSteps(userId: Long, date: String, count: Int) {
        val existing = getByDate(userId, date)
        if (existing != null) {
            addSteps(userId, date, count)
        } else {
            insert(DailySteps(userId = userId, date = date, count = count))
        }
    }
}
