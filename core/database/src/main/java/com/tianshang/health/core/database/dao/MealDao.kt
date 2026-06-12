package com.tianshang.health.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tianshang.health.core.database.entity.MealRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: MealRecord): Long

    @Update
    suspend fun update(meal: MealRecord)

    @Delete
    suspend fun delete(meal: MealRecord)

    @Query("SELECT * FROM meal_records WHERE id = :mealId")
    suspend fun getById(mealId: Long): MealRecord?

    @Query("SELECT * FROM meal_records WHERE userId = :userId AND date = :date ORDER BY createdAt ASC")
    fun getByDate(userId: Long, date: String): Flow<List<MealRecord>>

    @Query("SELECT * FROM meal_records WHERE userId = :userId AND date = :date ORDER BY createdAt ASC")
    suspend fun getByDateOnce(userId: Long, date: String): List<MealRecord>

    @Query("SELECT * FROM meal_records WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getAllByUser(userId: Long): Flow<List<MealRecord>>

    @Query(
        "SELECT * FROM meal_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date, createdAt"
    )
    suspend fun getByDateRange(userId: Long, startDate: String, endDate: String): List<MealRecord>

    @Query("SELECT SUM(calories) FROM meal_records WHERE userId = :userId AND date = :date")
    suspend fun getDailyCalories(userId: Long, date: String): Float?

    @Query("SELECT SUM(proteinGrams) FROM meal_records WHERE userId = :userId AND date = :date")
    suspend fun getDailyProtein(userId: Long, date: String): Float?

    @Query("SELECT SUM(carbsGrams) FROM meal_records WHERE userId = :userId AND date = :date")
    suspend fun getDailyCarbs(userId: Long, date: String): Float?

    @Query("SELECT SUM(fatGrams) FROM meal_records WHERE userId = :userId AND date = :date")
    suspend fun getDailyFat(userId: Long, date: String): Float?

    @Query("SELECT COUNT(*) FROM meal_records WHERE userId = :userId")
    suspend fun getCount(userId: Long): Int

    @Query("DELETE FROM meal_records WHERE id = :mealId")
    suspend fun deleteById(mealId: Long)

    @Query("DELETE FROM meal_records WHERE userId = :userId AND date = :date")
    suspend fun deleteByDate(userId: Long, date: String)

    @Query("SELECT DISTINCT date FROM meal_records WHERE userId = :userId ORDER BY date DESC")
    suspend fun getDistinctDates(userId: Long): List<String>
}
