package com.tianshang.health.feature.steps.data.repository

import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StepsRepositoryTest {

    private val stepsDao: StepsDao = mockk()
    private val userRepository: UserRepository = mockk()
    private lateinit var repository: StepsRepository

    private val testUser = User(id = 1, name = "Test", gender = "female")
    private val today = java.time.LocalDate.now().toString()

    @Before
    fun setUp() {
        coEvery { userRepository.getOrCreateDefault() } returns testUser
        coEvery { stepsDao.getByDate(1, today) } returns null
        repository = StepsRepository(stepsDao, userRepository)
    }

    @Test
    fun initialize_sets_currentUserId_and_loads_today_steps() = runTest {
        val existing = DailySteps(userId = 1, date = today, count = 500)
        coEvery { stepsDao.getByDate(1, today) } returns existing

        repository.initialize()

        assert(repository.getCurrentTodaySteps() == 500)
    }

    @Test
    fun initialize_sets_zero_when_no_steps_for_today() = runTest {
        coEvery { stepsDao.getByDate(1, today) } returns null

        repository.initialize()

        assert(repository.getCurrentTodaySteps() == 0)
    }

    @Test
    fun addSteps_increments_counter_and_updates_existing() = runTest {
        coEvery { userRepository.getOrCreateDefault() } returns testUser
        coEvery { stepsDao.getByDate(1, today) } returns DailySteps(userId = 1, date = today, count = 100)
        coEvery { stepsDao.addSteps(any(), any(), any(), any()) } returns Unit

        repository.initialize()
        repository.addSteps(50)

        assert(repository.getCurrentTodaySteps() == 150)
        coVerify { stepsDao.addSteps(eq(1), eq(today), eq(50), any()) }
    }

    @Test
    fun addSteps_creates_new_entry_when_none_exists() = runTest {
        coEvery { userRepository.getOrCreateDefault() } returns testUser
        coEvery { stepsDao.getByDate(1, today) } returns null
        coEvery { stepsDao.insert(any<DailySteps>()) } returns 1L

        repository.initialize()
        repository.addSteps(200)

        coVerify { stepsDao.insert(match { it.count == 200 }) }
    }

    @Test
    fun addSteps_updates_existing_entry() = runTest {
        coEvery { userRepository.getOrCreateDefault() } returns testUser
        coEvery { stepsDao.getByDate(1, today) } returns DailySteps(userId = 1, date = today, count = 300)
        coEvery { stepsDao.addSteps(any(), any(), any(), any()) } returns Unit

        repository.initialize()
        repository.addSteps(100)

        coVerify { stepsDao.addSteps(eq(1), eq(today), eq(100), any()) }
    }

    @Test
    fun updateGoal_creates_new_entry_when_none_exists_for_today() = runTest {
        coEvery { stepsDao.getByDate(1, today) } returns null
        coEvery { stepsDao.insert(any<DailySteps>()) } returns 1L

        repository.initialize()
        repository.updateGoal(10000)

        coVerify { stepsDao.insert(match { it.goal == 10000 && it.count == 0 }) }
    }

    @Test
    fun updateGoal_updates_existing_entry() = runTest {
        val existing = DailySteps(userId = 1, date = today, count = 500, goal = 5000)
        coEvery { stepsDao.getByDate(1, today) } returns existing
        coEvery { stepsDao.update(any<DailySteps>()) } returns Unit

        repository.initialize()
        repository.updateGoal(8000)

        coVerify { stepsDao.update(match { it.goal == 8000 }) }
    }

    @Test
    fun getTodaySteps_returns_flow_from_dao() = runTest {
        val steps = DailySteps(userId = 1, date = today, count = 1000)
        coEvery { stepsDao.getByDateFlow(1, today) } returns flowOf(steps)

        repository.initialize()
        val result = repository.getTodaySteps()

        assert(result != null)
    }

    @Test
    fun getTodayStepsSync_returns_from_dao() = runTest {
        val steps = DailySteps(userId = 1, date = today, count = 1000)
        coEvery { stepsDao.getByDate(1, today) } returns steps

        repository.initialize()
        val result = repository.getTodayStepsSync()

        assert(result != null)
        assert(result!!.count == 1000)
    }

    @Test
    fun getTotalSteps_returns_sum_from_dao() = runTest {
        val start = java.time.LocalDate.now().minusDays(7)
        val end = java.time.LocalDate.now()
        coEvery { stepsDao.getTotalSteps(1, start.toString(), end.toString()) } returns 35000

        repository.initialize()
        val result = repository.getTotalSteps(start, end)

        assert(result == 35000)
    }

    @Test
    fun getTotalSteps_returns_zero_when_dao_returns_null() = runTest {
        val start = java.time.LocalDate.now().minusDays(7)
        val end = java.time.LocalDate.now()
        coEvery { stepsDao.getTotalSteps(1, start.toString(), end.toString()) } returns null

        repository.initialize()
        val result = repository.getTotalSteps(start, end)

        assert(result == 0)
    }

    @Test
    fun getAverageSteps_returns_avg_from_dao() = runTest {
        val start = java.time.LocalDate.now().minusDays(7)
        val end = java.time.LocalDate.now()
        coEvery { stepsDao.getAverageSteps(1, start.toString(), end.toString()) } returns 5000f

        repository.initialize()
        val result = repository.getAverageSteps(start, end)

        assert(result == 5000f)
    }

    @Test
    fun getTotalStepsAllTime_returns_sum() = runTest {
        coEvery { stepsDao.getTotalStepsAllTime(1) } returns 100000

        repository.initialize()
        val result = repository.getTotalStepsAllTime()

        assert(result == 100000)
    }
}
