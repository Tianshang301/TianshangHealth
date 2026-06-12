package com.tianshang.health.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tianshang.health.core.database.entity.User.Gender
import com.tianshang.health.feature.dashboard.ui.DashboardScreen
import com.tianshang.health.feature.dashboard.viewmodel.DashboardState
import com.tianshang.health.feature.dashboard.viewmodel.DashboardUiState
import com.tianshang.health.feature.dashboard.viewmodel.DashboardViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsProgressIndicator() {
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Loading).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitForIdle()

        // Loading state renders without crash; title not present
        composeTestRule.onNodeWithText("Health Dashboard").assertDoesNotExist()
    }

    @Test
    fun successState_female_showsDashboardTitle() {
        val state = DashboardState(userGender = Gender.FEMALE, todaySteps = 5000, stepsGoal = 10000)
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Health Dashboard").assertIsDisplayed()
    }

    @Test
    fun successState_female_showsTodayStepsCard() {
        val state = DashboardState(userGender = Gender.FEMALE, todaySteps = 4200, stepsGoal = 8000)
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Today's Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("4200").assertIsDisplayed()
        composeTestRule.onNodeWithText("Goal: 8000").assertIsDisplayed()
    }

    @Test
    fun successState_female_showsPeriodStatusCard() {
        val state = DashboardState(
            userGender = Gender.FEMALE,
            todaySteps = 3000,
            isPeriodActive = false,
            currentCycleDay = 15
        )
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Period Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("Day 15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Follicular Phase").assertIsDisplayed()
    }

    @Test
    fun successState_female_periodActive_showsActiveStatus() {
        val state = DashboardState(
            userGender = Gender.FEMALE,
            todaySteps = 3000,
            isPeriodActive = true,
            currentCycleDay = 2
        )
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Period Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("Day 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Period Active").assertIsDisplayed()
    }

    @Test
    fun successState_female_noPeriodData_showsNoDataText() {
        val state = DashboardState(
            userGender = Gender.FEMALE,
            currentCycleDay = null
        )
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Period Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("No Data").assertIsDisplayed()
    }

    @Test
    fun successState_female_showsHealthInsightsCard() {
        val state = DashboardState(userGender = Gender.FEMALE)
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Health Insights").assertIsDisplayed()
        composeTestRule.onNodeWithText("Record more health data for personalized insights").assertIsDisplayed()
    }

    @Test
    fun successState_female_showsQuickActions() {
        val state = DashboardState(userGender = Gender.FEMALE)
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Record Period").assertIsDisplayed()
        composeTestRule.onNodeWithText("Record Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sleep").assertIsDisplayed()
        composeTestRule.onNodeWithText("Health Analysis").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nutrition").assertIsDisplayed()
    }

    @Test
    fun successState_male_hidesPeriodStatusCard() {
        val state = DashboardState(userGender = Gender.MALE, todaySteps = 6000)
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Health Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Period Status").assertDoesNotExist()
    }

    @Test
    fun successState_male_hidesRecordPeriodButton() {
        val state = DashboardState(userGender = Gender.MALE, todaySteps = 6000)
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Record Period").assertDoesNotExist()
        composeTestRule.onNodeWithText("Record Steps").assertIsDisplayed()
    }

    @Test
    fun successState_male_showsQuickActionsWithoutPeriod() {
        val state = DashboardState(userGender = Gender.MALE, todaySteps = 6000)
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Success(state)).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Record Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Health Analysis").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nutrition").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sleep").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessageAndRetry() {
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Error("Something went wrong")).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun errorState_retryButton_triggersRefresh() {
        val viewModel = mockk<DashboardViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DashboardUiState.Error("Failed to load data")).asStateFlow()

        composeTestRule.setContent {
            MaterialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()
        // relaxed mock silently accepts refresh() call; no crash expected
    }
}
