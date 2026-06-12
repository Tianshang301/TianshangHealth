package com.tianshang.health.feature.fitness.data.repository

import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.dao.WorkoutDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.entity.WorkoutRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class FitnessRepositoryTest {

    private val workoutDao: WorkoutDao = mockk()
    private val userDao: UserDao = mockk()
    private val dailyHealthDao: DailyHealthDao = mockk()
    private val stepsDao: StepsDao = mockk()
    private lateinit var repository: FitnessRepository

    private val testUser = User(id = 1, name = "Test", gender = "female", heightCm = 165f)
    private val today = LocalDate.now().toString()

    @Before
    fun setUp() = runTest {
        coEvery { userDao.getFirst() } returns testUser
        repository = FitnessRepository(workoutDao, userDao, dailyHealthDao, stepsDao)
        repository.initialize()
    }

    @Test
    fun initialize_loads_current_user() = runTest {
        val freshRepository = FitnessRepository(workoutDao, userDao, dailyHealthDao, stepsDao)
        freshRepository.initialize()
    }

    @Test
    fun addWorkout_inserts_record_and_returns_id() = runTest {
        val workoutId = 42L
        coEvery { workoutDao.insert(any<WorkoutRecord>()) } returns workoutId

        val result = repository.addWorkout(
            exerciseType = "RUNNING",
            durationMinutes = 30,
            caloriesBurned = 250f,
            date = today
        )

        assert(result == workoutId)
        coVerify { workoutDao.insert(match { it.exerciseType == "RUNNING" && it.durationMinutes == 30 }) }
    }

    @Test
    fun deleteWorkout_calls_dao_delete() = runTest {
        val record = WorkoutRecord(
            id = 1,
            userId = 1,
            date = today,
            exerciseType = "YOGA",
            durationMinutes = 20
        )
        coEvery { workoutDao.delete(any<WorkoutRecord>()) } returns Unit

        repository.deleteWorkout(record)

        coVerify { workoutDao.delete(record) }
    }

    @Test
    fun updateWorkout_calls_dao_update() = runTest {
        val record = WorkoutRecord(
            id = 1,
            userId = 1,
            date = today,
            exerciseType = "YOGA",
            durationMinutes = 20,
            notes = "Updated"
        )
        coEvery { workoutDao.update(any<WorkoutRecord>()) } returns Unit

        repository.updateWorkout(record)

        coVerify { workoutDao.update(record) }
    }

    @Test
    fun getWorkoutsByDate_returns_flow_from_dao() = runTest {
        val records = listOf(
            WorkoutRecord(id = 1, userId = 1, date = today, exerciseType = "RUNNING", durationMinutes = 30)
        )
        coEvery { workoutDao.getByDate(1, today) } returns flowOf(records)

        repository.getWorkoutsByDate(today)

        coVerify { workoutDao.getByDate(1, today) }
    }

    @Test
    fun getTotalDurationThisWeek_returns_duration_from_dao() = runTest {
        coEvery { workoutDao.getTotalDuration(any(), any(), any()) } returns 120

        val result = repository.getTotalDurationThisWeek()

        assert(result == 120)
    }

    @Test
    fun getTotalCaloriesThisWeek_returns_calories_from_dao() = runTest {
        coEvery { workoutDao.getTotalCalories(any(), any(), any()) } returns 500f

        val result = repository.getTotalCaloriesThisWeek()

        assert(result == 500f)
    }

    @Test
    fun getTodaySteps_returns_steps_from_dao() = runTest {
        coEvery { stepsDao.getByDate(1, today) } returns DailySteps(userId = 1, date = today, count = 3000)

        val result = repository.getTodaySteps()

        assert(result == 3000)
    }

    @Test
    fun getTodaySteps_returns_zero_when_no_record() = runTest {
        coEvery { stepsDao.getByDate(1, today) } returns null

        val result = repository.getTodaySteps()

        assert(result == 0)
    }

    @Test
    fun getTodayGoal_returns_default_when_no_record() = runTest {
        coEvery { stepsDao.getByDate(1, today) } returns null

        val result = repository.getTodayGoal()

        assert(result == 6000)
    }

    @Test
    fun saveWeight_inserts_when_no_existing_record() = runTest {
        coEvery { dailyHealthDao.getByDate(1, today) } returns null
        coEvery { dailyHealthDao.insert(any<DailyHealth>()) } returns 1L

        repository.saveWeight(65f, today)

        coVerify { dailyHealthDao.insert(match { it.weightKg == 65f }) }
    }

    @Test
    fun saveWeight_updates_when_existing_record() = runTest {
        val existing = DailyHealth(userId = 1, date = today, weightKg = 70f)
        coEvery { dailyHealthDao.getByDate(1, today) } returns existing
        coEvery { dailyHealthDao.update(any<DailyHealth>()) } returns Unit

        repository.saveWeight(65f, today)

        coVerify { dailyHealthDao.update(match { it.weightKg == 65f }) }
    }

    @Test
    fun getTotalStepsAllTime_returns_from_dao() = runTest {
        coEvery { stepsDao.getTotalStepsAllTime(1) } returns 100000

        val result = repository.getTotalStepsAllTime()

        assert(result == 100000)
    }

    @Test
    fun getWorkoutCountThisWeek_returns_from_dao() = runTest {
        coEvery { workoutDao.getCount(any(), any(), any()) } returns 5

        val result = repository.getWorkoutCountThisWeek()

        assert(result == 5)
    }

    @Test
    fun getUserHeight_returns_from_user() = runTest {
        val result = repository.getUserHeight()

        assert(result == 165f)
    }
}
