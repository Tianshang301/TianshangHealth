package com.tianshang.health.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tianshang.health.core.database.entity.PredictionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionLogDao {

    @Insert
    suspend fun insert(log: PredictionLog): Long

    @Update
    suspend fun update(log: PredictionLog)

    @Query("SELECT * FROM prediction_logs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getByUserId(userId: Long): Flow<List<PredictionLog>>

    @Query(
        "SELECT * FROM prediction_logs WHERE userId = :userId AND actualStartDate IS NULL ORDER BY createdAt DESC LIMIT 1"
    )
    suspend fun getMostRecentUnresolved(userId: Long): PredictionLog?

    @Query(
        "SELECT * FROM prediction_logs WHERE userId = :userId AND actualStartDate IS NOT NULL ORDER BY createdAt DESC LIMIT :limit"
    )
    suspend fun getRecentResolved(userId: Long, limit: Int): List<PredictionLog>

    @Query("SELECT AVG(ABS(errorDays)) FROM prediction_logs WHERE userId = :userId AND errorDays IS NOT NULL")
    suspend fun getMeanAbsoluteError(userId: Long): Float?

    @Query(
        "SELECT COUNT(*) FROM prediction_logs WHERE userId = :userId AND errorDays IS NOT NULL AND ABS(errorDays) <= :threshold"
    )
    suspend fun getAccuracyWithinThreshold(userId: Long, threshold: Int): Int

    @Query("SELECT COUNT(*) FROM prediction_logs WHERE userId = :userId AND errorDays IS NOT NULL")
    suspend fun getResolvedCount(userId: Long): Int

    @Query(
        "SELECT * FROM prediction_logs WHERE userId = :userId AND tfliteModelUsed IS NOT NULL ORDER BY createdAt DESC"
    )
    fun getTfliteLogs(userId: Long): Flow<List<PredictionLog>>

    @Query(
        "SELECT AVG(ABS(errorDays)) FROM prediction_logs WHERE userId = :userId AND errorDays IS NOT NULL AND tfliteModelUsed = :modelName"
    )
    suspend fun getMeanAbsoluteErrorByModel(userId: Long, modelName: String): Float?

    @Query(
        "SELECT * FROM prediction_logs WHERE userId = :userId AND tfliteModelUsed IS NOT NULL AND actualStartDate IS NULL ORDER BY createdAt DESC LIMIT 1"
    )
    suspend fun getMostRecentTfliteUnresolved(userId: Long): PredictionLog?

    @Query(
        "SELECT COUNT(*) FROM prediction_logs WHERE userId = :userId AND tfliteModelUsed IS NOT NULL AND tfliteModelUsed = :modelName"
    )
    suspend fun getTflitePredictionCount(userId: Long, modelName: String): Int

    @Query("DELETE FROM prediction_logs WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Long)
}
