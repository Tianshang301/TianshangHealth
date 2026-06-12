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
    fun saveSleep_updates_existing_record() = runTest {
        val existing = DailyHealth(userId = 1, date = today, sleepHours = 6f, deepSleepHours = 1.5f, sleepQuality = 3)
        coEvery { dailyHealthDao.getByDate(1, today) } returns existing
        coEvery { dailyHealthDao.update(any<DailyHealth>()) } returns Unit

        repository.saveSleep(today, sleepHours = 8f, deepSleepHours = 2f, sleepQuality = 5)

        coVerify {
            dailyHealthDao.update(
                match {
                    it.sleepHours == 8f && it.deepSleepHours == 2f && it.sleepQuality == 5
                }
            )
        }
    }

    @Test
    fun saveSleep_inserts_when_no_existing_record() = runTest {
        coEvery { dailyHealthDao.getByDate(1, today) } returns null
        coEvery { dailyHealthDao.insert(any<DailyHealth>()) } returns 1L

        repository.saveSleep(today, sleepHours = 7.5f, deepSleepHours = 2f, sleepQuality = 4)

        coVerify {
            dailyHealthDao.insert(
                match {
                    it.sleepHours == 7.5f && it.deepSleepHours == 2f && it.sleepQuality == 4
                }
            )
        }
    }

    @Test
    fun saveSleep_partial_update_keeps_existing_values() = runTest {
        val existing = DailyHealth(userId = 1, date = today, sleepHours = 6f, deepSleepHours = 1.5f, sleepQuality = 3)
        coEvery { dailyHealthDao.getByDate(1, today) } returns existing
        coEvery { dailyHealthDao.update(any<DailyHealth>()) } returns Unit

        repository.saveSleep(today, sleepHours = null, deepSleepHours = null, sleepQuality = 5)

        coVerify {
            dailyHealthDao.update(
                match {
                    it.sleepHours == 6f && it.deepSleepHours == 1.5f && it.sleepQuality == 5
                }
            )
        }
    }
}
