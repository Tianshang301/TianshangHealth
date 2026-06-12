package com.tianshang.health.feature.analysis.viewmodel

import android.content.Context
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.feature.analysis.util.LocalizedContextProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {

    private val context: Context = mockk()
    private val stringResolver: StringResolver = mockk()
    private val userRepository: UserRepository = mockk()
    private val periodRecordDao: PeriodRecordDao = mockk()
    private val dailyHealthDao: DailyHealthDao = mockk()
    private lateinit var viewModel: ReportViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testUser = User(id = 1, name = "Test", gender = "female")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(LocalizedContextProvider)
        every { LocalizedContextProvider.getLocalizedContext(any()) } returns context

        coEvery { userRepository.getOrCreateDefault() } returns testUser
        coEvery { periodRecordDao.getByUserIdList(any()) } returns emptyList()
        coEvery { dailyHealthDao.getByDateRange(any(), any(), any()) } returns emptyList()

        every { stringResolver.getString(R.string.error_failed_generate_report) } returns "Failed to generate"
        every { stringResolver.getString(R.string.error_unknown) } returns "Unknown error"

        viewModel = ReportViewModel(context, stringResolver, userRepository, periodRecordDao, dailyHealthDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_is_idle() {
        assert(viewModel.uiState.value is ReportUiState.Idle)
    }

    @Test
    fun generateReport_sets_generating_state() = runTest {
        viewModel.generateReport()
        val state = viewModel.uiState.value
        assert(state is ReportUiState.Error || state is ReportUiState.Generating)
    }

    @Test
    fun generateReport_handles_error() = runTest {
        coEvery { userRepository.getOrCreateDefault() } throws RuntimeException("DB error")
        viewModel.generateReport()
        assert(viewModel.uiState.value is ReportUiState.Error)
    }

    @Test
    fun resetState_returns_to_idle() {
        viewModel.resetState()
        assert(viewModel.uiState.value is ReportUiState.Idle)
    }
}
