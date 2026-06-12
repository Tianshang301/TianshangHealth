package com.tianshang.health.feature.onboarding.viewmodel

import android.content.Context
import android.content.SharedPreferences
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.core.security.encryption.KeystoreManager
import com.tianshang.health.feature.onboarding.model.Gender
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val context: Context = mockk()
    private val stringResolver: StringResolver = mockk()
    private val userRepository: UserRepository = mockk()
    private val prefs: SharedPreferences = mockk()
    private lateinit var viewModel: OnboardingViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(KeystoreManager)
        every { KeystoreManager.getEncryptedSharedPreferences(context) } returns prefs
        every { prefs.getBoolean("onboarding_completed", false) } returns false
        every { prefs.edit() } returns mockk {
            every { putBoolean(any(), any()) } returns this
            every { putString(any(), any()) } returns this
            every { apply() } returns Unit
        }

        coEvery { userRepository.getFirst() } returns null
        coEvery { userRepository.insert(any<User>()) } returns 1L

        viewModel = OnboardingViewModel(context, stringResolver, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_is_loading_then_success_not_completed() = runTest(testDispatcher) {
        val state = viewModel.uiState.value
        assert(state is OnboardingUiState.Success)
        val successState = state as OnboardingUiState.Success
        assert(!successState.state.isCompleted)
        assert(!successState.state.isLoading)
    }

    @Test
    fun selectGender_updates_state_and_calls_repository() = runTest(testDispatcher) {
        coEvery { userRepository.insert(any<User>()) } returns 1L

        viewModel.selectGender(Gender.FEMALE)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state is OnboardingUiState.Success)
        val successState = state as OnboardingUiState.Success
        assert(successState.state.isCompleted)
        assert(successState.state.selectedGender == Gender.FEMALE)
        coVerify { userRepository.insert(any()) }
    }

    @Test
    fun selectGender_male_saves_correctly() = runTest(testDispatcher) {
        coEvery { userRepository.insert(any<User>()) } returns 1L

        viewModel.selectGender(Gender.MALE)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state is OnboardingUiState.Success)
        val successState = state as OnboardingUiState.Success
        assert(successState.state.selectedGender == Gender.MALE)
        coVerify { userRepository.insert(any()) }
    }
}
