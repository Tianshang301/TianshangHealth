package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import android.content.SharedPreferences
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.core.security.encryption.KeystoreManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val context: Context = mockk()
    private val userRepository: UserRepository = mockk()
    private val sharedPrefs: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk()

    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testUser = User(id = 1, name = "Test", gender = "female", dateOfBirth = "1990-01-15")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(KeystoreManager)
        every { KeystoreManager.getEncryptedSharedPreferences(context) } returns sharedPrefs
        every { sharedPrefs.getString(any(), any()) } returns null
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        coEvery { userRepository.getAll() } returns MutableStateFlow(listOf(testUser))
        coEvery { userRepository.getOrCreateDefault() } returns testUser
        coEvery { userRepository.updateGender(any(), any()) } returns Unit
        coEvery { userRepository.updateDateOfBirth(any(), any()) } returns Unit

        viewModel = ProfileViewModel(context, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleGenderDialog_flips_state() {
        val before = viewModel.showGenderDialog.value
        viewModel.toggleGenderDialog()
        assert(viewModel.showGenderDialog.value != before)
        viewModel.toggleGenderDialog()
        assert(viewModel.showGenderDialog.value == before)
    }

    @Test
    fun updateGender_saves_to_repository_and_prefs() = runTest {
        viewModel.updateGender(User.Gender.MALE)
        coVerify { userRepository.updateGender(1, User.Gender.MALE) }
        verify { editor.putString("b2p5q8r1", "male") }
    }

    @Test
    fun updateDateOfBirth_calls_repository() = runTest {
        viewModel.updateDateOfBirth("1995-06-15")
        coVerify { userRepository.updateDateOfBirth(1, "1995-06-15") }
    }

    @Test
    fun updateGender_dismisses_dialog() = runTest {
        viewModel.toggleGenderDialog()
        assert(viewModel.showGenderDialog.value)
        viewModel.updateGender(User.Gender.MALE)
        assert(!viewModel.showGenderDialog.value)
    }

    @Test
    fun showGenderDialog_defaults_to_false() {
        assert(!viewModel.showGenderDialog.value)
    }
}
