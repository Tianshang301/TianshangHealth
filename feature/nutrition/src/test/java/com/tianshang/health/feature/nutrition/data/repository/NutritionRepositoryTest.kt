package com.tianshang.health.feature.nutrition.data.repository

import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.MealDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.MealRecord
import com.tianshang.health.core.database.entity.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NutritionRepositoryTest {

    private val mealDao: MealDao = mockk()
    private val userDao: UserDao = mockk()
    private val dailyHealthDao: DailyHealthDao = mockk()
    private val periodRecordDao: PeriodRecordDao = mockk()
    private lateinit var repository: NutritionRepository

    private val testUser = User(id = 1, name = "Test", gender = "female")
    private val testDate = "2026-06-05"

    @Before
    fun setUp() {
        coEvery { userDao.getFirst() } returns testUser
        repository = NutritionRepository(mealDao, userDao, dailyHealthDao, periodRecordDao)
    }

    @Test
    fun `addMeal inserts meal and syncs daily health`() = runTest {
        coEvery { mealDao.insert(any<MealRecord>()) } returns 1L
        coEvery { mealDao.getDailyCalories(1, testDate) } returns 500f
        coEvery { mealDao.getDailyProtein(1, testDate) } returns 20f
        coEvery { mealDao.getDailyCarbs(1, testDate) } returns 60f
        coEvery { mealDao.getDailyFat(1, testDate) } returns 10f
        coEvery { dailyHealthDao.getByDate(1, testDate) } returns null
        coEvery { dailyHealthDao.insert(any<DailyHealth>()) } returns 1L

        repository.initialize()
        val id = repository.addMeal("breakfast", "Oatmeal", 250f, date = testDate)

        assert(id == 1L)
        coVerify { mealDao.insert(any<MealRecord>()) }
        coVerify { dailyHealthDao.insert(any<DailyHealth>()) }
    }

    @Test
    fun `addMeal throws when no user`() = runTest {
        coEvery { userDao.getFirst() } returns null

        repository.initialize()
        try {
            repository.addMeal("breakfast", "Oatmeal", date = testDate)
            assert(false) { "Expected exception" }
        } catch (e: IllegalStateException) {
            assert(e.message?.contains("No user found") == true)
        }
    }

    @Test
    fun `getDailyCalories returns correct sum`() = runTest {
        coEvery { mealDao.getDailyCalories(1, testDate) } returns 750f

        repository.initialize()
        val calories = repository.getDailyCalories(testDate)

        assert(calories == 750f)
        coVerify { mealDao.getDailyCalories(1, testDate) }
    }

    @Test
    fun `getDailyCalories returns zero when no meals`() = runTest {
        coEvery { mealDao.getDailyCalories(1, testDate) } returns null

        repository.initialize()
        val calories = repository.getDailyCalories(testDate)

        assert(calories == 0f)
    }

    @Test
    fun `getDailyWaterIntake returns existing value`() = runTest {
        val dailyHealth = DailyHealth(userId = 1, date = testDate, waterIntake = 1000f)
        coEvery { dailyHealthDao.getByDate(1, testDate) } returns dailyHealth

        repository.initialize()
        val water = repository.getDailyWaterIntake(testDate)

        assert(water == 1000f)
    }

    @Test
    fun `getDailyWaterIntake returns zero when no record`() = runTest {
        coEvery { dailyHealthDao.getByDate(1, testDate) } returns null

        repository.initialize()
        val water = repository.getDailyWaterIntake(testDate)

        assert(water == 0f)
    }

    @Test
    fun `addWater increments water intake`() = runTest {
        val existingHealth = DailyHealth(userId = 1, date = testDate, waterIntake = 500f)
        coEvery { dailyHealthDao.getByDate(1, testDate) } returns existingHealth
        coEvery { dailyHealthDao.update(any<DailyHealth>()) } returns Unit

        repository.initialize()
        val newTotal = repository.addWater(250f, testDate)

        assert(newTotal == 750f)
        coVerify { dailyHealthDao.update(match { it.waterIntake == 750f }) }
    }

    @Test
    fun `deleteMeal deletes meal and syncs daily health`() = runTest {
        val meal =
            MealRecord(id = 1, userId = 1, date = testDate, mealType = "breakfast", foodName = "Eggs", calories = 300f)
        coEvery { mealDao.delete(meal) } returns Unit
        coEvery { mealDao.getDailyCalories(1, testDate) } returns 0f
        coEvery { mealDao.getDailyProtein(1, testDate) } returns 0f
        coEvery { mealDao.getDailyCarbs(1, testDate) } returns 0f
        coEvery { mealDao.getDailyFat(1, testDate) } returns 0f
        coEvery { dailyHealthDao.getByDate(1, testDate) } returns null

        repository.initialize()
        repository.deleteMeal(meal)

        coVerify { mealDao.delete(meal) }
    }

    @Test
    fun `getMealsByDate returns meals from dao`() = runTest {
        val meals = listOf(
            MealRecord(userId = 1, date = testDate, mealType = "breakfast", foodName = "Toast")
        )
        coEvery { mealDao.getByDate(1, testDate) } returns flowOf(meals)

        repository.initialize()
        val result = repository.getMealsByDate(testDate).first()

        assert(result == meals)
        coVerify { mealDao.getByDate(1, testDate) }
    }

    @Test
    fun `getMealsByDate returns empty list from dao`() = runTest {
        coEvery { mealDao.getByDate(1, testDate) } returns flowOf(emptyList())

        repository.initialize()
        val result = repository.getMealsByDate(testDate).first()

        assert(result.isEmpty())
    }
}
