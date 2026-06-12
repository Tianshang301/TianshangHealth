package com.tianshang.health.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tianshang.health.core.database.entity.WorkoutRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WorkoutRecord): Long

    @Update
    suspend fun update(record: WorkoutRecord)

    @Delete
    suspend fun delete(record: WorkoutRecord)

    @Query("SELECT * FROM workout_records WHERE id = :id")
    suspend fun getById(id: Long): WorkoutRecord?

    @Query("SELECT * FROM workout_records WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    fun getByDate(userId: Long, date: String): Flow<List<WorkoutRecord>>

    @Query("SELECT * FROM workout_records WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    suspend fun getByDateOnce(userId: Long, date: String): List<WorkoutRecord>

    @Query("SELECT * FROM workout_records WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getAllByUser(userId: Long): Flow<List<WorkoutRecord>>

    @Query(
        "SELECT * FROM workout_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC"
    )
    suspend fun getByDateRange(userId: Long, startDate: String, endDate: String): List<WorkoutRecord>

    @Query(
        "SELECT * FROM workout_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC"
    )
    fun getByDateRangeFlow(userId: Long, startDate: String, endDate: String): Flow<List<WorkoutRecord>>

    @Query(
        "SELECT SUM(durationMinutes) FROM workout_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate"
    )
    suspend fun getTotalDuration(userId: Long, startDate: String, endDate: String): Int?

    @Query(
        "SELECT SUM(caloriesBurned) FROM workout_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate"
    )
    suspend fun getTotalCalories(userId: Long, startDate: String, endDate: String): Float?

    @Query("SELECT COUNT(*) FROM workout_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getCount(userId: Long, startDate: String, endDate: String): Int

    @Query("DELETE FROM workout_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT DISTINCT exerciseType FROM workout_records WHERE userId = :userId ORDER BY exerciseType")
    suspend fun getDistinctExerciseTypes(userId: Long): List<String>

    @Query("SELECT SUM(caloriesBurned) FROM workout_records WHERE userId = :userId AND date = :date")
    suspend fun getDailyCalories(userId: Long, date: String): Float?

    @Query(
        "SELECT SUM(caloriesBurned) FROM workout_records WHERE userId = :userId AND date >= :startDate AND date <= :endDate"
    )
    suspend fun getCaloriesByDateRange(userId: Long, startDate: String, endDate: String): Float?

    @Query(
        "SELECT caloriesBurned FROM workout_records WHERE userId = :userId AND date >= :startDate AND date <= :endDate AND caloriesBurned IS NOT NULL ORDER BY date"
    )
    suspend fun getCaloriesList(userId: Long, startDate: String, endDate: String): List<Float>

    @Query(
        "SELECT date, SUM(caloriesBurned) as totalCalories FROM workout_records WHERE userId = :userId AND date >= :startDate AND date <= :endDate AND caloriesBurned IS NOT NULL GROUP BY date ORDER BY date"
    )
    suspend fun getDailyCaloriesByDateRange(
        userId: Long,
        startDate: String,
        endDate: String
    ): List<DailyCaloriesSummary>

    @Query(
        "SELECT exerciseType, SUM(caloriesBurned) as totalCalories FROM workout_records WHERE userId = :userId AND date >= :startDate AND date <= :endDate AND caloriesBurned IS NOT NULL GROUP BY exerciseType ORDER BY totalCalories DESC"
    )
    suspend fun getCaloriesByExerciseType(
        userId: Long,
        startDate: String,
        endDate: String
    ): List<ExerciseCaloriesSummary>
}

data class DailyCaloriesSummary(
    val date: String,
    val totalCalories: Float
)

data class ExerciseCaloriesSummary(
    val exerciseType: String,
    val totalCalories: Float
)
