package com.tianshang.health.feature.dashboard.viewmodel

import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.entity.DailySteps
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.PeriodRecordRepository
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.feature.dashboard.domain.GetHealthInsightsUseCase
import com.tianshang.health.feature.dashboard.domain.InsightResult
import com.tianshang.health.feature.onboarding.model.Gender
import com.tianshang.health.feature.steps.data.repository.StepsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val stringResolver: StringResolver = mockk(relaxed = true)

    private val userDao: UserDao = mockk()
    private val userRepository = UserRepository(userDao)

    private val stepsDao: StepsDao = mockk()
    private val stepsRepository = StepsRepository(stepsDao, userRepository)

    private val periodRecordRepository: PeriodRecordRepository = mockk()
    private val getHealthInsights: GetHealthInsightsUseCase = mockk()
    private lateinit var viewModel: DashboardViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testUser = User(id = 1, name = "Test", gender = "female")
    private val today = LocalDate.now()
    private val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { userDao.getFirst() } returns testUser
        coEvery { userDao.insert(any()) } returns 1L
        every { userDao.getAll() } returns flowOf(listOf(testUser))

        coEvery {
            stepsDao.getByDate(any(), any())
        } returns DailySteps(userId = 1, date = today.toString(), count = 5000, goal = 6000)
        every {
            stepsDao.getByDateFlow(any(), any())
        } returns flowOf(DailySteps(userId = 1, date = today.toString(), count = 5000, goal = 6000))
        every { stepsDao.getByDateRangeFlow(any(), any(), any()) } returns flowOf(emptyList())
        every { stepsDao.getRecentFlow(any(), any()) } returns flowOf(emptyList())

        coEvery { periodRecordRepository.getRecentRecords(1, 5) } returns emptyList()
        coEvery { periodRecordRepository.getActiveRecordOnDate(1, today.toString()) } returns null
        coEvery { getHealthInsights(any()) } returns InsightResult.EMPTY

        // ViewModel created in each test method inside runTest
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_is_loading_then_success() = runTest {
        viewModel = DashboardViewModel(stringResolver, userRepository, stepsRepository, periodRecordRepository, getHealthInsights)

        val state = viewModel.uiState.value
        assert(state is DashboardUiState.Success)
        val successState = state as DashboardUiState.Success
        assert(successState.state.userGender == Gender.FEMALE)
        assert(successState.state.todaySteps == 5000)
        assert(successState.state.stepsGoal == 6000)
        assert(!successState.state.isLoading)
    }

    @Test
    fun refresh_calls_observeDashboard() = runTest {
        viewModel = DashboardViewModel(stringResolver, userRepository, stepsRepository, periodRecordRepository, getHealthInsights)

        viewModel.refresh()

        val state = viewModel.uiState.value
        assert(state is DashboardUiState.Success)
    }

    @Test
    fun male_user_hides_period_data() = runTest {
        val maleUser = User(id = 1, name = "Test", gender = "male")
        coEvery { userDao.getFirst() } returns maleUser
        coEvery { userDao.getAll() } returns flowOf(listOf(maleUser))

        viewModel = DashboardViewModel(stringResolver, userRepository, stepsRepository, periodRecordRepository, getHealthInsights)

        val state = viewModel.uiState.value
        assert(state is DashboardUiState.Success)
        val successState = state as DashboardUiState.Success
        assert(successState.state.userGender == Gender.MALE)
        assert(successState.state.recentPeriodRecords.isEmpty())
        assert(!successState.state.isPeriodActive)
        assert(successState.state.currentCycleDay == null)
    }

    @Test
    fun error_state_when_repository_fails() = runTest {
        coEvery { userDao.getFirst() } throws RuntimeException("Network error")

        viewModel = DashboardViewModel(stringResolver, userRepository, stepsRepository, periodRecordRepository, getHealthInsights)

        val state = viewModel.uiState.value
        assert(state is DashboardUiState.Error)
    }
}
