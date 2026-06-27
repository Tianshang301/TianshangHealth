package com.tianshang.health.feature.period.viewmodel

import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.PeriodRecordRepository
import com.tianshang.health.core.database.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class RecycleBinViewModelTest {

    private val periodRecordRepository: PeriodRecordRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val stringResolver: StringResolver = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: RecycleBinViewModel

    private val testUser = User(id = 1, name = "Test", gender = "female", dateOfBirth = "1990-01-15")
    private val testRecords = listOf(
        PeriodRecord(id = 10, userId = 1, startDate = "2026-01-10", isDeleted = true),
        PeriodRecord(id = 11, userId = 1, startDate = "2026-02-14", isDeleted = true)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { userRepository.getOrCreateDefault() } returns testUser
        coEvery { periodRecordRepository.getDeletedRecordsList(testUser.id) } returns testRecords
        coEvery { periodRecordRepository.restore(any()) } returns Unit
        coEvery { periodRecordRepository.deleteById(any()) } returns Unit
        every { stringResolver.getString(R.string.error_unknown) } returns "Unknown error"
        every { stringResolver.getString(R.string.error_failed_restore) } returns "Failed to restore"
        every { stringResolver.getString(R.string.error_failed_delete) } returns "Failed to delete"
        every { stringResolver.getString(R.string.error_failed_empty_bin) } returns "Failed to empty bin"

        viewModel = RecycleBinViewModel(periodRecordRepository, userRepository, stringResolver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading_thenLoadsRecords() {
        val state = viewModel.uiState.value
        assert(state is RecycleBinUiState.Success)
        assert((state as RecycleBinUiState.Success).records.size == 2)
        assert(state.records[0].id == 10L)
        assert(state.records[1].id == 11L)
    }

    @Test
    fun loadDeletedRecords_setsError_whenNoUser() {
        coEvery { userRepository.getOrCreateDefault() } throws RuntimeException("User not found")
        val errorViewModel = RecycleBinViewModel(periodRecordRepository, userRepository, stringResolver)
        val state = errorViewModel.uiState.value
        assert(state is RecycleBinUiState.Error)
        assert((state as RecycleBinUiState.Error).message == "Unknown error")
    }

    @Test
    fun loadDeletedRecords_setsError_whenRepositoryFails() {
        coEvery { periodRecordRepository.getDeletedRecordsList(any()) } throws RuntimeException("DB error")
        val errorViewModel = RecycleBinViewModel(periodRecordRepository, userRepository, stringResolver)
        val state = errorViewModel.uiState.value
        assert(state is RecycleBinUiState.Error)
        assert((state as RecycleBinUiState.Error).message == "Unknown error")
    }

    @Test
    fun restore_callsRepositoryAndRefreshes() = runTest {
        viewModel.restore(10L)
        coVerify { periodRecordRepository.restore(10L) }
        coVerify { periodRecordRepository.getDeletedRecordsList(1L) }
        assert(viewModel.uiState.value is RecycleBinUiState.Success)
    }

    @Test
    fun permanentlyDelete_callsDeleteByIdAndRefreshes() = runTest {
        viewModel.permanentlyDelete(10L)
        coVerify { periodRecordRepository.deleteById(10L) }
        coVerify { periodRecordRepository.getDeletedRecordsList(1L) }
        assert(viewModel.uiState.value is RecycleBinUiState.Success)
    }

    @Test
    fun emptyBin_deletesAllAndRefreshes() = runTest {
        viewModel.emptyBin()
        coVerify { periodRecordRepository.getDeletedRecordsList(1L) }
        coVerify { periodRecordRepository.deleteById(10L) }
        coVerify { periodRecordRepository.deleteById(11L) }
        assert(viewModel.uiState.value is RecycleBinUiState.Success)
    }

    @Test
    fun emptyBin_setsError_onFailure() = runTest {
        coEvery { periodRecordRepository.deleteById(any()) } throws RuntimeException("Delete failed")
        viewModel.emptyBin()
        val state = viewModel.uiState.value
        assert(state is RecycleBinUiState.Error)
        assert((state as RecycleBinUiState.Error).message == "Failed to empty bin")
    }
}
