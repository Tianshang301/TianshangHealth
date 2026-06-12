package com.tianshang.health.core.database.repository

import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.entity.PeriodRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PeriodRecordRepositoryTest {

    private val periodRecordDao: PeriodRecordDao = mockk()
    private lateinit var repository: PeriodRecordRepository

    private val testRecord = PeriodRecord(
        id = 1,
        userId = 1,
        startDate = "2026-06-01",
        endDate = "2026-06-05",
        flowLevel = 2,
        painLevel = 1
    )

    @Before
    fun setUp() {
        repository = PeriodRecordRepository(periodRecordDao)
    }

    @Test
    fun `getByUserId returns flow from dao`() = runTest {
        coEvery { periodRecordDao.getByUserId(1) } returns flowOf(listOf(testRecord))

        val result = repository.getByUserId(1).first()

        assert(result.size == 1)
        assert(result[0].startDate == "2026-06-01")
    }

    @Test
    fun `getByUserId returns empty list when no records`() = runTest {
        coEvery { periodRecordDao.getByUserId(1) } returns flowOf(emptyList())

        val result = repository.getByUserId(1).first()

        assert(result.isEmpty())
    }

    @Test
    fun `getByUserIdList returns records from dao`() = runTest {
        coEvery { periodRecordDao.getByUserIdList(1) } returns listOf(testRecord)

        val result = repository.getByUserIdList(1)

        assert(result.size == 1)
        assert(result[0].id == 1L)
    }

    @Test
    fun `getByUserIdList returns empty list when none exist`() = runTest {
        coEvery { periodRecordDao.getByUserIdList(1) } returns emptyList()

        val result = repository.getByUserIdList(1)

        assert(result.isEmpty())
    }

    @Test
    fun `getById returns record from dao`() = runTest {
        coEvery { periodRecordDao.getById(1) } returns testRecord

        val result = repository.getById(1)

        assert(result != null)
        assert(result!!.startDate == "2026-06-01")
    }

    @Test
    fun `getById returns null for non-existent record`() = runTest {
        coEvery { periodRecordDao.getById(999) } returns null

        val result = repository.getById(999)

        assert(result == null)
    }

    @Test
    fun `insert returns id from dao`() = runTest {
        coEvery { periodRecordDao.insert(testRecord) } returns 42L

        val id = repository.insert(testRecord)

        assert(id == 42L)
    }

    @Test
    fun `update delegates to dao`() = runTest {
        coEvery { periodRecordDao.update(testRecord) } returns Unit

        repository.update(testRecord)

        coVerify { periodRecordDao.update(testRecord) }
    }

    @Test
    fun `deleteById delegates to dao`() = runTest {
        coEvery { periodRecordDao.deleteById(1) } returns Unit

        repository.deleteById(1)

        coVerify { periodRecordDao.deleteById(1) }
    }

    @Test
    fun `getActiveRecordOnDate returns record from dao`() = runTest {
        coEvery { periodRecordDao.getActiveRecordOnDate(1, "2026-06-02") } returns testRecord

        val result = repository.getActiveRecordOnDate(1, "2026-06-02")

        assert(result != null)
        assert(result!!.id == 1L)
    }

    @Test
    fun `getActiveRecordOnDate returns null when no active record`() = runTest {
        coEvery { periodRecordDao.getActiveRecordOnDate(1, "2026-07-01") } returns null

        val result = repository.getActiveRecordOnDate(1, "2026-07-01")

        assert(result == null)
    }

    @Test
    fun `softDelete calls dao with timestamp`() = runTest {
        coEvery { periodRecordDao.softDelete(any(), any()) } returns Unit

        repository.softDelete(1)

        coVerify { periodRecordDao.softDelete(eq(1), any()) }
    }

    @Test
    fun `restore calls dao with timestamp`() = runTest {
        coEvery { periodRecordDao.restore(any(), any()) } returns Unit

        repository.restore(1)

        coVerify { periodRecordDao.restore(eq(1), any()) }
    }

    @Test
    fun `getDeletedRecords returns flow from dao`() = runTest {
        val deletedRecord = testRecord.copy(isDeleted = true)
        coEvery { periodRecordDao.getDeletedRecords(1) } returns flowOf(listOf(deletedRecord))

        val result = repository.getDeletedRecords(1).first()

        assert(result.size == 1)
        assert(result[0].isDeleted == true)
    }

    @Test
    fun `getCount returns number from dao`() = runTest {
        coEvery { periodRecordDao.getCount(1) } returns 5

        val count = repository.getCount(1)

        assert(count == 5)
    }

    @Test
    fun `getRecentRecords returns limited records`() = runTest {
        coEvery { periodRecordDao.getRecentRecords(1, 5) } returns listOf(testRecord)

        val result = repository.getRecentRecords(1, 5)

        assert(result.size == 1)
        coVerify { periodRecordDao.getRecentRecords(1, 5) }
    }

    @Test
    fun `permanentlyDeleteOld removes old records`() = runTest {
        val cutoff = System.currentTimeMillis()
        coEvery { periodRecordDao.permanentlyDeleteOld(cutoff) } returns Unit

        repository.permanentlyDeleteOld(cutoff)

        coVerify { periodRecordDao.permanentlyDeleteOld(cutoff) }
    }
}
