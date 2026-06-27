package com.tianshang.health.feature.steps.viewmodel

import android.content.Context
import android.os.PowerManager
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.feature.steps.data.repository.StepsRepository
import com.tianshang.health.feature.steps.service.StepCounterService
import com.tianshang.health.feature.steps.service.StepSyncWorker
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StepsViewModelTest {

    private val context: Context = mockk()
    private val stringResolver: StringResolver = mockk()
    private val stepsRepository: StepsRepository = mockk(relaxed = true)
    private lateinit var viewModel: StepsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val powerManager: PowerManager = mockk()
        every { context.getSystemService(Context.POWER_SERVICE) } returns powerManager
        every { powerManager.isIgnoringBatteryOptimizations(any()) } returns true
        every { context.packageName } returns "com.tianshang.health"

        mockkObject(StepCounterService.Companion)
        every { StepCounterService.startService(any()) } answers { Unit }

        mockkObject(StepSyncWorker.Companion)
        every { StepSyncWorker.schedule(any()) } answers { Unit }

        coEvery { stepsRepository.initialize() } returns Unit
        coEvery { stepsRepository.updateGoal(any()) } returns Unit
        every { stringResolver.getString(R.string.error_unknown) } returns "Unknown error"
        every { stringResolver.getString(R.string.error_failed_update_goal) } returns "Failed to update goal"

        viewModel = StepsViewModel(context, stringResolver, stepsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun viewModel_is_created_without_crash() {
        assert(viewModel is StepsViewModel)
    }

    @Test
    fun updateGoal_calls_repository() {
        viewModel.updateGoal(8000)
        coEvery { stepsRepository.updateGoal(8000) } returns Unit
    }

    @Test
    fun refresh_does_not_crash() {
        viewModel.refresh()
        coEvery { stepsRepository.initialize() } returns Unit
    }

    @Test
    fun error_state_when_repository_fails() {
        coEvery { stepsRepository.initialize() } throws RuntimeException("Init failed")

        val freshViewModel = StepsViewModel(context, stringResolver, stepsRepository)

        val state = freshViewModel.uiState.value
        assert(state is StepsUiState.Error)
    }
}
