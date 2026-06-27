package com.tianshang.health.feature.onboarding.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.core.security.encryption.KeystoreManager
import com.tianshang.health.feature.onboarding.model.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val isCompleted: Boolean = false,
    val selectedGender: Gender? = null,
    val isLoading: Boolean = true
)

sealed class OnboardingUiState {
    object Loading : OnboardingUiState()
    data class Success(val state: OnboardingState) : OnboardingUiState()
    data class Error(val message: String) : OnboardingUiState()
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stringResolver: StringResolver,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Loading)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "c3s6t9"
        private const val KEY_USER_GENDER = "g5h1j7"
    }

    private val prefs = KeystoreManager.getEncryptedSharedPreferences(context)

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            try {
                val isCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

                if (isCompleted) {
                    _uiState.value = OnboardingUiState.Success(
                        OnboardingState(isCompleted = true, isLoading = false)
                    )
                } else {
                    _uiState.value = OnboardingUiState.Success(
                        OnboardingState(isCompleted = false, isLoading = false)
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: stringResolver.getString(R.string.error_unknown))
            }
        }
    }

    fun selectGender(gender: Gender) {
        viewModelScope.launch {
            try {
                _uiState.value = OnboardingUiState.Success(
                    OnboardingState(selectedGender = gender, isLoading = true)
                )

                // Create or update user with gender
                val existingUser = userRepository.getFirst()
                if (existingUser != null) {
                    userRepository.update(existingUser.copy(gender = gender.value))
                } else {
                    val user = User(name = "User", gender = gender.value)
                    userRepository.insert(user)
                }

                // Save onboarding completed status
                prefs.edit()
                    .putBoolean(KEY_ONBOARDING_COMPLETED, true)
                    .putString(KEY_USER_GENDER, gender.value)
                    .apply()

                _uiState.value = OnboardingUiState.Success(
                    OnboardingState(
                        isCompleted = true,
                        selectedGender = gender,
                        isLoading = false
                    )
                )
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: stringResolver.getString(R.string.error_failed_save_gender))
            }
        }
    }

    fun getUserGender(): Gender {
        val genderValue = prefs.getString(KEY_USER_GENDER, Gender.FEMALE.value) ?: Gender.FEMALE.value
        return Gender.fromValue(genderValue)
    }
}
