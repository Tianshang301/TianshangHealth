package com.tianshang.health.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tianshang.health.core.database.entity.PeriodRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PeriodRecord): Long

    @Update
    suspend fun update(record: PeriodRecord)

    @androidx.room.Delete
    suspend fun delete(record: PeriodRecord)

    @Query("SELECT * FROM period_records WHERE id = :recordId AND isDeleted = 0")
    suspend fun getById(recordId: Long): PeriodRecord?

    @Query("SELECT * FROM period_records WHERE id = :recordId")
    fun getByIdFlow(recordId: Long): Flow<PeriodRecord?>

    @Query("SELECT * FROM period_records WHERE userId = :userId AND isDeleted = 0 ORDER BY startDate DESC")
    fun getByUserId(userId: Long): Flow<List<PeriodRecord>>

    @Query("SELECT * FROM period_records WHERE userId = :userId AND isDeleted = 0 ORDER BY startDate DESC")
    suspend fun getByUserIdList(userId: Long): List<PeriodRecord>

    @Query(
        "SELECT * FROM period_records WHERE userId = :userId AND isDeleted = 0 AND startDate <= :date AND (endDate IS NULL OR endDate >= :date) LIMIT 1"
    )
    suspend fun getActiveRecordOnDate(userId: Long, date: String): PeriodRecord?

    @Query("SELECT * FROM period_records WHERE userId = :userId AND isDeleted = 0 ORDER BY startDate DESC LIMIT :limit")
    suspend fun getRecentRecords(userId: Long, limit: Int): List<PeriodRecord>

    @Query(
        "SELECT * FROM period_records WHERE userId = :userId AND isDeleted = 0 AND startDate >= :startDate ORDER BY startDate ASC"
    )
    suspend fun getRecordsSince(userId: Long, startDate: String): List<PeriodRecord>

    @Query("SELECT COUNT(*) FROM period_records WHERE userId = :userId AND isDeleted = 0")
    suspend fun getCount(userId: Long): Int

    // Soft delete
    @Query(
        "UPDATE period_records SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :recordId"
    )
    suspend fun softDelete(recordId: Long, deletedAt: Long = System.currentTimeMillis())

    // Restore from recycle bin
    @Query("UPDATE period_records SET isDeleted = 0, deletedAt = NULL, updatedAt = :restoredAt WHERE id = :recordId")
    suspend fun restore(recordId: Long, restoredAt: Long = System.currentTimeMillis())

    // Get deleted records (recycle bin)
    @Query("SELECT * FROM period_records WHERE userId = :userId AND isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedRecords(userId: Long): Flow<List<PeriodRecord>>

    @Query("SELECT * FROM period_records WHERE userId = :userId AND isDeleted = 1 ORDER BY deletedAt DESC")
    suspend fun getDeletedRecordsList(userId: Long): List<PeriodRecord>

    // Permanently delete old records (older than 30 days)
    @Query("DELETE FROM period_records WHERE isDeleted = 1 AND deletedAt < :cutoffTime")
    suspend fun permanentlyDeleteOld(cutoffTime: Long)

    // Hard delete (for permanent deletion)
    @Query("DELETE FROM period_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)
}
