package com.tianshang.health.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tianshang.health.core.database.entity.DailySymptom
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySymptomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(symptom: DailySymptom): Long

    @Update
    suspend fun update(symptom: DailySymptom)

    @Delete
    suspend fun delete(symptom: DailySymptom)

    @Query("SELECT * FROM daily_symptoms WHERE id = :symptomId")
    suspend fun getById(symptomId: Long): DailySymptom?

    @Query("SELECT * FROM daily_symptoms WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getByDate(userId: Long, date: String): DailySymptom?

    @Query("SELECT * FROM daily_symptoms WHERE userId = :userId AND date = :date LIMIT 1")
    fun getByDateFlow(userId: Long, date: String): Flow<DailySymptom?>

    @Query("SELECT * FROM daily_symptoms WHERE userId = :userId ORDER BY date DESC")
    fun getByUserId(userId: Long): Flow<List<DailySymptom>>

    @Query(
        "SELECT * FROM daily_symptoms WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC"
    )
    suspend fun getByDateRange(userId: Long, startDate: String, endDate: String): List<DailySymptom>

    @Query("SELECT * FROM daily_symptoms WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(userId: Long, limit: Int): List<DailySymptom>

    @Query("SELECT COUNT(*) FROM daily_symptoms WHERE userId = :userId")
    suspend fun getCount(userId: Long): Int

    @Query("DELETE FROM daily_symptoms WHERE id = :symptomId")
    suspend fun deleteById(symptomId: Long)
}
