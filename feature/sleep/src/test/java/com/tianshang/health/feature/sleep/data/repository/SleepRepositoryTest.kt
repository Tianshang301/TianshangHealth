package com.tianshang.health.feature.sleep.data.repository

import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SleepRepositoryTest {

    private val dailyHealthDao: DailyHealthDao = mockk()
    private val userDao: UserDao = mockk()
    private lateinit var repository: SleepRepository

    private val testUser = User(id = 1, name = "Test", gender = "female")
    private val today = LocalDate.now().toString()

    @Before
    fun setUp() = runTest {
        coEvery { userDao.getFirst() } returns testUser
        repository = SleepRepository(dailyHealthDao, userDao)
        repository.initialize()
    }

    @Test
    fun getTodaySleep_returns_daily_health_from_dao() = runTest {
        val expected = DailyHealth(userId = 1, date = today, sleepHours = 7.5f)
        coEvery { dailyHealthDao.getByDate(1, today) } returns expected

        val result = repository.getTodaySleep()

        assert(result?.sleepHours == 7.5f)
    }

    @Test
    fun getTodaySleep_returns_null_when_no_record() = runTest {
        coEvery { dailyHealthDao.getByDate(1, today) } returns null

        val result = repository.getTodaySleep()

        assert(result == null)
    }

    @Test
    fun getRecentDays_returns_list_from_dao() = runTest {
        val records = listOf(
            DailyHealth(userId = 1, date = LocalDate.now().minusDays(1).toString(), sleepHours = 8f),
            DailyHealth(userId = 1, date = today, sleepHours = 7f)
        )
        coEvery { dailyHealthDao.getByDateRange(any(), any(), any()) } returns records

        val result = repository.getRecentDays(2)

        assert(result.size == 2)
    }

    @Test
    fun saveSleep_delegates_to_dao() = runTest {
        coEvery {
            dailyHealthDao.insertOrUpdateSleep(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Unit

        repository.saveSleep(today, sleepHours = 8f, deepSleepHours = 2f, sleepQuality = 5)

        coVerify {
            dailyHealthDao.insertOrUpdateSleep(
                eq(
                    1L
                ),
                eq(today), eq(8f), eq(2f), eq(5), any(), any(), any(), any()
            )
        }
    }

    @Test
    fun saveSleep_passes_bedTime_wakeTime_to_dao() = runTest {
        coEvery {
            dailyHealthDao.insertOrUpdateSleep(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Unit

        repository.saveSleep(
            today,
            sleepHours = 7f,
            deepSleepHours = 2f,
            sleepQuality = 4,
            bedTime = "23:00",
            wakeTime = "07:00",
            sleepLatency = 15,
            wakeCount = 1
        )

        coVerify {
            dailyHealthDao.insertOrUpdateSleep(
                eq(1L), eq(today), eq(7f), eq(2f), eq(4),
                eq("23:00"), eq("07:00"), eq(15), eq(1)
            )
        }
    }

    @Test
    fun saveSleep_handles_partial_null_params() = runTest {
        coEvery {
            dailyHealthDao.insertOrUpdateSleep(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Unit

        repository.saveSleep(
            today,
            sleepHours = null,
            deepSleepHours = null,
            sleepQuality = 5,
            bedTime = "22:30"
        )

        coVerify {
            dailyHealthDao.insertOrUpdateSleep(
                eq(1L), eq(today), any(), any(), eq(5),
                eq("22:30"), any(), any(), any()
            )
        }
    }

    @Test
    fun getSleepConsistency_returns_null_with_insufficient_data() = runTest {
        val records = listOf(
            DailyHealth(userId = 1, date = LocalDate.now().minusDays(1).toString(), sleepHours = 7f, bedTime = "23:00"),
            DailyHealth(userId = 1, date = today, sleepHours = 8f, bedTime = "22:30")
        )
        coEvery { dailyHealthDao.getByDateRange(any(), any(), any()) } returns records

        val result = repository.getSleepConsistency(2)

        assert(result == null)
    }

    @Test
    fun getSleepConsistency_returns_high_score_with_consistent_data() = runTest {
        val records = listOf(
            DailyHealth(
                userId = 1,
                date = LocalDate.now().minusDays(4).toString(),
                sleepHours = 7.5f,
                bedTime = "23:00",
                wakeTime = "06:30"
            ),
            DailyHealth(
                userId = 1,
                date = LocalDate.now().minusDays(3).toString(),
                sleepHours = 7f,
                bedTime = "23:15",
                wakeTime = "06:15"
            ),
            DailyHealth(
                userId = 1,
                date = LocalDate.now().minusDays(2).toString(),
                sleepHours = 8f,
                bedTime = "23:00",
                wakeTime = "07:00"
            ),
            DailyHealth(
                userId = 1,
                date = LocalDate.now().minusDays(1).toString(),
                sleepHours = 7.8f,
                bedTime = "22:45",
                wakeTime = "06:45"
            ),
            DailyHealth(
                userId = 1,
                date = today,
                sleepHours = 7.2f,
                bedTime = "23:00",
                wakeTime = "06:30"
            )
        )
        coEvery { dailyHealthDao.getByDateRange(any(), any(), any()) } returns records

        val result = repository.getSleepConsistency(7)

        assert(result != null)
        assert(result!!.overallScore > 70f)
    }

    @Test
    fun getDateRange_returns_data_from_dao() = runTest {
        val records = listOf(
            DailyHealth(userId = 1, date = "2026-06-01", sleepHours = 7f),
            DailyHealth(userId = 1, date = "2026-06-02", sleepHours = 8f)
        )
        coEvery { dailyHealthDao.getByDateRange(1, "2026-06-01", "2026-06-02") } returns records

        val result = repository.getDateRange("2026-06-01", "2026-06-02")

        assert(result.size == 2)
    }
}
