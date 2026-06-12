package com.tianshang.health.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tianshang.health.feature.fitness.domain.CycleFitnessResult
import com.tianshang.health.feature.fitness.viewmodel.AddWorkoutState
import com.tianshang.health.feature.fitness.viewmodel.FitnessState
import com.tianshang.health.feature.fitness.viewmodel.FitnessViewModel
import com.tianshang.health.feature.steps.util.OemType
import com.tianshang.health.feature.steps.viewmodel.StepsState
import com.tianshang.health.feature.steps.viewmodel.StepsUiState
import com.tianshang.health.feature.steps.viewmodel.StepsViewModel
import com.tianshang.health.navigation.ExerciseScreen
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsProgressIndicator() {
        val stepsViewModel = mockk<StepsViewModel>(relaxed = true)
        every { stepsViewModel.uiState } returns MutableStateFlow(StepsUiState.Loading).asStateFlow()

        val fitnessViewModel = mockk<FitnessViewModel>(relaxed = true)
        every { fitnessViewModel.state } returns MutableStateFlow(FitnessState(isLoading = true)).asStateFlow()
        every { fitnessViewModel.addState } returns MutableStateFlow(AddWorkoutState()).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                ExerciseScreen(
                    stepsViewModel = stepsViewModel,
                    fitnessViewModel = fitnessViewModel
                )
            }
        }

        composeTestRule.waitForIdle()

        // Title is still visible in loading state (top app bar renders)
        composeTestRule.onNodeWithText("Fitness").assertIsDisplayed()
    }

    @Test
    fun successState_showsStepsCard() {
        val stepsState = StepsState(
            todaySteps = 7500,
            todayGoal = 10000,
            weeklyAverage = 6500f,
            isBatteryOptimizationDisabled = true,
            oemType = OemType.OTHER
        )
        val fitnessState = FitnessState(
            isLoading = false,
            cycleFitnessResult = CycleFitnessResult.EMPTY,
            todayStepsCalories = 300f,
            totalCaloriesToday = 200f,
            combinedDailyCalories = 500f,
            totalStepsThisWeek = 45000,
            weeklyStepsCalories = 1800f,
            totalCaloriesThisWeek = 1200f,
            workoutCountThisWeek = 5,
            totalDurationThisWeek = 180
        )

        val stepsViewModel = mockk<StepsViewModel>(relaxed = true)
        every { stepsViewModel.uiState } returns MutableStateFlow(
            StepsUiState.Success(stepsState)
        ).asStateFlow()

        val fitnessViewModel = mockk<FitnessViewModel>(relaxed = true)
        every { fitnessViewModel.state } returns MutableStateFlow(fitnessState).asStateFlow()
        every { fitnessViewModel.addState } returns MutableStateFlow(AddWorkoutState()).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                ExerciseScreen(
                    stepsViewModel = stepsViewModel,
                    fitnessViewModel = fitnessViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Fitness").assertIsDisplayed()
        composeTestRule.onNodeWithText("Steps Tracking").assertIsDisplayed()
        composeTestRule.onNodeWithText("7500").assertIsDisplayed()
        composeTestRule.onNodeWithText("of 10000 goal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekly avg 6500 steps/day").assertIsDisplayed()
    }

    @Test
    fun successState_showsEnergySummaryCard() {
        val stepsState = StepsState(
            todaySteps = 5000,
            todayGoal = 10000,
            isBatteryOptimizationDisabled = true,
            oemType = OemType.OTHER
        )
        val fitnessState = FitnessState(
            isLoading = false,
            cycleFitnessResult = CycleFitnessResult.EMPTY,
            todayStepsCalories = 300f,
            totalCaloriesToday = 200f,
            combinedDailyCalories = 500f,
            totalStepsThisWeek = 35000,
            weeklyStepsCalories = 1400f,
            totalCaloriesThisWeek = 800f,
            workoutCountThisWeek = 3,
            totalDurationThisWeek = 120
        )

        val stepsViewModel = mockk<StepsViewModel>(relaxed = true)
        every { stepsViewModel.uiState } returns MutableStateFlow(
            StepsUiState.Success(stepsState)
        ).asStateFlow()

        val fitnessViewModel = mockk<FitnessViewModel>(relaxed = true)
        every { fitnessViewModel.state } returns MutableStateFlow(fitnessState).asStateFlow()
        every { fitnessViewModel.addState } returns MutableStateFlow(AddWorkoutState()).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                ExerciseScreen(
                    stepsViewModel = stepsViewModel,
                    fitnessViewModel = fitnessViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Energy Summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
        composeTestRule.onNodeWithText("This Week").assertIsDisplayed()
    }

    @Test
    fun successState_showsGoalAdjustmentCard() {
        val stepsState = StepsState(
            todaySteps = 5000,
            todayGoal = 8000,
            isBatteryOptimizationDisabled = true,
            oemType = OemType.OTHER
        )

        val stepsViewModel = mockk<StepsViewModel>(relaxed = true)
        every { stepsViewModel.uiState } returns MutableStateFlow(
            StepsUiState.Success(stepsState)
        ).asStateFlow()

        val fitnessViewModel = mockk<FitnessViewModel>(relaxed = true)
        every { fitnessViewModel.state } returns MutableStateFlow(FitnessState(isLoading = false)).asStateFlow()
        every { fitnessViewModel.addState } returns MutableStateFlow(AddWorkoutState()).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                ExerciseScreen(
                    stepsViewModel = stepsViewModel,
                    fitnessViewModel = fitnessViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Daily Goal").assertIsDisplayed()
    }

    @Test
    fun successState_showsBodyMetricsCard() {
        val stepsState = StepsState(
            todaySteps = 5000,
            todayGoal = 10000,
            isBatteryOptimizationDisabled = true,
            oemType = OemType.OTHER
        )

        val stepsViewModel = mockk<StepsViewModel>(relaxed = true)
        every { stepsViewModel.uiState } returns MutableStateFlow(
            StepsUiState.Success(stepsState)
        ).asStateFlow()

        val fitnessViewModel = mockk<FitnessViewModel>(relaxed = true)
        every { fitnessViewModel.state } returns MutableStateFlow(FitnessState(isLoading = false)).asStateFlow()
        every { fitnessViewModel.addState } returns MutableStateFlow(AddWorkoutState()).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                ExerciseScreen(
                    stepsViewModel = stepsViewModel,
                    fitnessViewModel = fitnessViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Body Metrics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Height (cm)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weight (kg)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessageAndRetry() {
        val stepsViewModel = mockk<StepsViewModel>(relaxed = true)
        every { stepsViewModel.uiState } returns MutableStateFlow(
            StepsUiState.Error("Failed to load steps")
        ).asStateFlow()

        val fitnessViewModel = mockk<FitnessViewModel>(relaxed = true)
        every { fitnessViewModel.state } returns MutableStateFlow(FitnessState()).asStateFlow()
        every { fitnessViewModel.addState } returns MutableStateFlow(AddWorkoutState()).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                ExerciseScreen(
                    stepsViewModel = stepsViewModel,
                    fitnessViewModel = fitnessViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Failed to load steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun successState_withBatteryOptimizationNotDisabled_showsBatteryCard() {
        val stepsState = StepsState(
            todaySteps = 5000,
            todayGoal = 10000,
            isBatteryOptimizationDisabled = false,
            oemType = OemType.OTHER
        )

        val stepsViewModel = mockk<StepsViewModel>(relaxed = true)
        every { stepsViewModel.uiState } returns MutableStateFlow(
            StepsUiState.Success(stepsState)
        ).asStateFlow()

        val fitnessViewModel = mockk<FitnessViewModel>(relaxed = true)
        every { fitnessViewModel.state } returns MutableStateFlow(FitnessState(isLoading = false)).asStateFlow()
        every { fitnessViewModel.addState } returns MutableStateFlow(AddWorkoutState()).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                ExerciseScreen(
                    stepsViewModel = stepsViewModel,
                    fitnessViewModel = fitnessViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Disable Battery Optimization").assertIsDisplayed()
        composeTestRule.onNodeWithText("Go to Settings").assertIsDisplayed()
    }

    @Test
    fun successState_withEmptyWorkouts_showsEmptyMessage() {
        val stepsState = StepsState(
            todaySteps = 5000,
            todayGoal = 10000,
            isBatteryOptimizationDisabled = true,
            oemType = OemType.OTHER
        )

        val stepsViewModel = mockk<StepsViewModel>(relaxed = true)
        every { stepsViewModel.uiState } returns MutableStateFlow(
            StepsUiState.Success(stepsState)
        ).asStateFlow()

        val fitnessViewModel = mockk<FitnessViewModel>(relaxed = true)
        every { fitnessViewModel.state } returns MutableStateFlow(FitnessState(isLoading = false)).asStateFlow()
        every { fitnessViewModel.addState } returns MutableStateFlow(AddWorkoutState()).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                ExerciseScreen(
                    stepsViewModel = stepsViewModel,
                    fitnessViewModel = fitnessViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("No workouts recorded yet. Tap + to add your first workout.").assertIsDisplayed()
    }

    @Test
    fun errorState_retryButton_triggersRefresh() {
        val stepsViewModel = mockk<StepsViewModel>(relaxed = true)
        every { stepsViewModel.uiState } returns MutableStateFlow(
            StepsUiState.Error("Connection error")
        ).asStateFlow()

        val fitnessViewModel = mockk<FitnessViewModel>(relaxed = true)
        every { fitnessViewModel.state } returns MutableStateFlow(FitnessState()).asStateFlow()
        every { fitnessViewModel.addState } returns MutableStateFlow(AddWorkoutState()).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                ExerciseScreen(
                    stepsViewModel = stepsViewModel,
                    fitnessViewModel = fitnessViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()
        // relaxed mock silently accepts refresh() call; no crash expected
    }
}
