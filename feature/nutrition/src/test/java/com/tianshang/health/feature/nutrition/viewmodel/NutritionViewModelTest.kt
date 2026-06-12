package com.tianshang.health.feature.nutrition.viewmodel

import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.MealRecord
import com.tianshang.health.feature.nutrition.data.repository.NutritionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModelTest {

    private val stringResolver: StringResolver = mockk(relaxed = true)
    private val repository: NutritionRepository = mockk(relaxed = true)
    private lateinit var viewModel: NutritionViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { repository.initialize() } returns Unit

        val mealsFlow = MutableStateFlow<List<MealRecord>>(emptyList())
        coEvery { repository.getMealsByDate(any()) } returns mealsFlow
        coEvery { repository.getAllMeals() } returns mealsFlow

        coEvery { repository.getDailyCalories(any()) } returns 0f
        coEvery { repository.getDailyProtein(any()) } returns 0f
        coEvery { repository.getDailyCarbs(any()) } returns 0f
        coEvery { repository.getDailyFat(any()) } returns 0f
        coEvery { repository.getMealCountToday() } returns 0
        coEvery { repository.getDailyWaterIntake(any()) } returns 0f
        coEvery { repository.getWeeklyCalories() } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads data and sets loading false`() = runTest(testDispatcher) {
        viewModel = NutritionViewModel(stringResolver, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assert(!state.isLoading)
    }

    @Test
    fun `saveMeal calls repository addMeal`() = runTest(testDispatcher) {
        coEvery { repository.addMeal(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns 1L
        coEvery { repository.getDailyCalories(any()) } returns 500f
        coEvery { repository.getDailyProtein(any()) } returns 20f
        coEvery { repository.getDailyCarbs(any()) } returns 60f
        coEvery { repository.getDailyFat(any()) } returns 10f
        coEvery { repository.getMealCountToday() } returns 1
        coEvery { repository.getDailyWaterIntake(any()) } returns 0f
        coEvery { repository.getWeeklyCalories() } returns emptyList()

        viewModel = NutritionViewModel(stringResolver, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateFoodName("Chicken Salad")
        viewModel.updateCalories("450")
        viewModel.updateProtein("30")
        viewModel.saveMeal()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.addMeal(any(), "Chicken Salad", 450f, 30f, any(), any(), any(), any(), any()) }
        val addState = viewModel.addState.value
        assert(addState.saveSuccess)
    }

    @Test
    fun `saveMeal shows error when food name is blank`() = runTest(testDispatcher) {
        viewModel = NutritionViewModel(stringResolver, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveMeal()
        testDispatcher.scheduler.advanceUntilIdle()

        val addState = viewModel.addState.value
        assert(addState.error != null)
        coVerify(inverse = true) { repository.addMeal(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `addWater calls repository and updates state`() = runTest(testDispatcher) {
        coEvery { repository.addWater(any<Float>(), any()) } returns 500f

        viewModel = NutritionViewModel(stringResolver, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addWater(250f)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.addWater(250f) }
        val state = viewModel.state.value
        assert(state.dailyWaterIntake == 500f)
    }

    @Test
    fun `deleteMeal calls repository delete`() = runTest(testDispatcher) {
        val meal = MealRecord(id = 1, userId = 1, date = "2026-06-05", mealType = "breakfast", foodName = "Eggs")
        coEvery { repository.deleteMeal(meal) } returns Unit
        coEvery { repository.getDailyCalories(any()) } returns 0f
        coEvery { repository.getDailyProtein(any()) } returns 0f
        coEvery { repository.getDailyCarbs(any()) } returns 0f
        coEvery { repository.getDailyFat(any()) } returns 0f
        coEvery { repository.getMealCountToday() } returns 0
        coEvery { repository.getDailyWaterIntake(any()) } returns 0f
        coEvery { repository.getWeeklyCalories() } returns emptyList()

        viewModel = NutritionViewModel(stringResolver, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteMeal(meal)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.deleteMeal(meal) }
    }

    @Test
    fun `updateWaterGoal updates state`() = runTest {
        viewModel = NutritionViewModel(stringResolver, repository)

        viewModel.updateWaterGoal(2500f)

        val state = viewModel.state.value
        assert(state.waterIntakeGoal == 2500f)
    }

    @Test
    fun `clearError resets error states`() = runTest(testDispatcher) {
        viewModel = NutritionViewModel(stringResolver, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateFoodName("")
        viewModel.saveMeal()
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.addState.value.error != null)

        viewModel.clearError()

        assert(viewModel.state.value.error == null)
        assert(viewModel.addState.value.error == null)
    }

    @Test
    fun `resetAddState resets add meal form`() = runTest(testDispatcher) {
        viewModel = NutritionViewModel(stringResolver, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateFoodName("Pasta")
        viewModel.updateCalories("400")
        assert(viewModel.addState.value.foodName == "Pasta")

        viewModel.resetAddState()

        assert(viewModel.addState.value.foodName == "")
        assert(viewModel.addState.value.calories == "")
    }
}
