package com.tianshang.health.feature.period.viewmodel

import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.PeriodRecordRepository
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.feature.period.engine.PredictionEngine
import io.mockk.coEvery
import io.mockk.coVerify
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
class PeriodViewModelTest {

    private val stringResolver: StringResolver = mockk()
    private val periodRecordRepository: PeriodRecordRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val predictionEngine: PredictionEngine = mockk()
    private val stepsDao: StepsDao = mockk(relaxed = true)
    private val dailyHealthDao: DailyHealthDao = mockk()
    private lateinit var viewModel: PeriodViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testUser = User(id = 1, name = "Test", gender = "female", heightCm = 165f)
    private val today = LocalDate.now()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { stringResolver.getString(any()) } answers {
            when (it.invocation.args[0] as Int) {
                R.string.insufficient_data -> "Insufficient data"
                R.string.loading -> "Loading"
                else -> "Error"
            }
        }

        coEvery { userRepository.getOrCreateDefault() } returns testUser
        every { userRepository.getAll() } returns flowOf(listOf(testUser))

        every { stepsDao.getByDateFlow(any(), any()) } returns flowOf(null)
        every { stepsDao.getRecentFlow(any(), any()) } returns flowOf(emptyList())
        every { stepsDao.getByUserId(any()) } returns flowOf(emptyList())
        every { stepsDao.getByDateRangeFlow(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { stepsDao.getByDate(any(), any()) } returns null

        coEvery { periodRecordRepository.getByUserIdList(1) } returns emptyList()
        coEvery { dailyHealthDao.getRecent(1, 30) } returns emptyList()
        coEvery { predictionEngine.predict(any()) } returns null
        coEvery { predictionEngine.calculateCycleRegularity(any()) } returns "Insufficient data"

        viewModel = PeriodViewModel(
            stringResolver, periodRecordRepository, userRepository,
            predictionEngine, stepsDao, dailyHealthDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_is_loading_then_success() = runTest {
        val state = viewModel.uiState.value
        assert(state is PeriodUiState.Success)
        val successState = state as PeriodUiState.Success
        assert(successState.periodState.records.isEmpty())
        assert(successState.periodState.prediction == null)
    }

    @Test
    fun addPeriodRecord_inserts_and_refreshes() = runTest {
        coEvery { periodRecordRepository.insert(any()) } returns 1L
        coEvery { periodRecordRepository.getByUserIdList(1) } returns listOf(
            PeriodRecord(id = 1, userId = 1, startDate = today.toString())
        )

        viewModel.addPeriodRecord(today, null, 3, 2, "Notes")

        coVerify { periodRecordRepository.insert(any()) }
        val state = viewModel.uiState.value
        assert(state is PeriodUiState.Success)
    }

    @Test
    fun deletePeriodRecord_deletes_and_refreshes() = runTest {
        coEvery { periodRecordRepository.deleteById(1) } returns Unit

        viewModel.deletePeriodRecord(1)

        coVerify { periodRecordRepository.deleteById(1) }
    }

    @Test
    fun selectDate_updates_selected_date() = runTest {
        val newDate = today.plusDays(5)
        viewModel.selectDate(newDate)

        assert(viewModel.selectedDate.value == newDate)
    }

    @Test
    fun stepsToKcal_calculates_with_height_and_weight() = runTest {
        coEvery { dailyHealthDao.getRecent(1, 30) } returns listOf(
            DailyHealth(userId = 1, date = today.toString(), weightKg = 60f)
        )

        viewModel = PeriodViewModel(
            stringResolver, periodRecordRepository, userRepository,
            predictionEngine, stepsDao, dailyHealthDao
        )

        val kcal = viewModel.stepsToKcal(10000)
        assert(kcal > 0f)
    }

    @Test
    fun isPeriodActive_returns_false_when_no_records() {
        assert(!viewModel.isPeriodActive())
    }

    @Test
    fun getPredictionExplanation_returns_not_enough_data() {
        val explanation = viewModel.getPredictionExplanation()
        assert(explanation.isNotEmpty())
    }

    @Test
    fun getCycleRegularity_returns_insufficient_data() {
        val regularity = viewModel.getCycleRegularity()
        assert(regularity == "Insufficient data")
    }
}
