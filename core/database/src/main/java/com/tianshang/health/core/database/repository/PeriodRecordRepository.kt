package com.tianshang.health.core.database.repository

import com.tianshang.health.core.common.util.ValidationUtils
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.entity.PeriodRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeriodRecordRepository @Inject constructor(
    private val periodRecordDao: PeriodRecordDao
) {

    fun getByUserId(userId: Long): Flow<List<PeriodRecord>> {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        return periodRecordDao.getByUserId(userId)
    }

    suspend fun getByUserIdList(userId: Long): List<PeriodRecord> {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        return periodRecordDao.getByUserIdList(userId)
    }

    suspend fun getById(recordId: Long): PeriodRecord? {
        require(ValidationUtils.isValidId(recordId)) { "Invalid recordId: $recordId" }
        return periodRecordDao.getById(recordId)
    }

    suspend fun insert(record: PeriodRecord): Long {
        require(ValidationUtils.isValidId(record.userId)) { "Invalid userId: ${record.userId}" }
        require(ValidationUtils.isValidDateString(record.startDate)) { "Invalid startDate: ${record.startDate}" }
        require(ValidationUtils.isValidDate(LocalDate.parse(record.startDate))) {
            "startDate cannot be in the future: ${record.startDate}"
        }
        if (record.endDate != null) {
            require(ValidationUtils.isValidDateString(record.endDate)) { "Invalid endDate: ${record.endDate}" }
            require(
                ValidationUtils.isValidDateRange(
                    LocalDate.parse(record.startDate),
                    LocalDate.parse(record.endDate)
                )
            ) { "endDate before startDate: ${record.endDate} < ${record.startDate}" }
        }
        require(ValidationUtils.isValidFlowLevel(record.flowLevel)) { "Invalid flowLevel: ${record.flowLevel}" }
        require(ValidationUtils.isValidPainLevel(record.painLevel)) { "Invalid painLevel: ${record.painLevel}" }
        return periodRecordDao.insert(record)
    }

    suspend fun update(record: PeriodRecord) {
        require(ValidationUtils.isValidId(record.id)) { "Invalid record id: ${record.id}" }
        require(ValidationUtils.isValidId(record.userId)) { "Invalid userId: ${record.userId}" }
        require(ValidationUtils.isValidDateString(record.startDate)) { "Invalid startDate: ${record.startDate}" }
        if (record.endDate != null) {
            require(ValidationUtils.isValidDateString(record.endDate)) { "Invalid endDate: ${record.endDate}" }
            require(
                ValidationUtils.isValidDateRange(
                    LocalDate.parse(record.startDate),
                    LocalDate.parse(record.endDate)
                )
            ) { "endDate before startDate: ${record.endDate} < ${record.startDate}" }
        }
        require(ValidationUtils.isValidFlowLevel(record.flowLevel)) { "Invalid flowLevel: ${record.flowLevel}" }
        require(ValidationUtils.isValidPainLevel(record.painLevel)) { "Invalid painLevel: ${record.painLevel}" }
        periodRecordDao.update(record)
    }

    suspend fun deleteById(recordId: Long) {
        require(ValidationUtils.isValidId(recordId)) { "Invalid recordId: $recordId" }
        periodRecordDao.deleteById(recordId)
    }

    suspend fun getActiveRecordOnDate(userId: Long, date: String): PeriodRecord? {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        require(ValidationUtils.isValidDateString(date)) { "Invalid date: $date" }
        return periodRecordDao.getActiveRecordOnDate(userId, date)
    }

    suspend fun getRecentRecords(userId: Long, limit: Int): List<PeriodRecord> {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        require(ValidationUtils.isValidLimit(limit)) { "Invalid limit: $limit" }
        return periodRecordDao.getRecentRecords(userId, limit)
    }

    suspend fun getRecordsSince(userId: Long, startDate: String): List<PeriodRecord> {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        require(ValidationUtils.isValidDateString(startDate)) { "Invalid startDate: $startDate" }
        return periodRecordDao.getRecordsSince(userId, startDate)
    }

    suspend fun getCount(userId: Long): Int {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        return periodRecordDao.getCount(userId)
    }

    suspend fun softDelete(recordId: Long) {
        require(ValidationUtils.isValidId(recordId)) { "Invalid recordId: $recordId" }
        periodRecordDao.softDelete(recordId, System.currentTimeMillis())
    }

    suspend fun restore(recordId: Long) {
        require(ValidationUtils.isValidId(recordId)) { "Invalid recordId: $recordId" }
        periodRecordDao.restore(recordId, System.currentTimeMillis())
    }

    fun getDeletedRecords(userId: Long): Flow<List<PeriodRecord>> {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        return periodRecordDao.getDeletedRecords(userId)
    }

    suspend fun getDeletedRecordsList(userId: Long): List<PeriodRecord> {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        return periodRecordDao.getDeletedRecordsList(userId)
    }

    suspend fun permanentlyDeleteOld(cutoffTime: Long) {
        require(cutoffTime > 0L) { "Invalid cutoffTime: $cutoffTime" }
        periodRecordDao.permanentlyDeleteOld(cutoffTime)
    }
}
