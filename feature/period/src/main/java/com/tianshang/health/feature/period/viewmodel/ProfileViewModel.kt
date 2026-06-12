package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.core.security.encryption.KeystoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) : ViewModel() {

    val currentGender: StateFlow<User.Gender> = userRepository.getAll()
        .map { users -> users.firstOrNull()?.let { User.Gender.fromValue(it.gender) } ?: User.Gender.OTHER }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), User.Gender.OTHER)

    val currentDateOfBirth: StateFlow<String?> = userRepository.getAll()
        .map { users -> users.firstOrNull()?.dateOfBirth }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showGenderDialog = MutableStateFlow(false)
    val showGenderDialog: StateFlow<Boolean> = _showGenderDialog.asStateFlow()

    private val _genderChanged = Channel<Unit>(Channel.CONFLATED)
    val genderChanged: Flow<Unit> = _genderChanged.receiveAsFlow()

    fun toggleGenderDialog() {
        _showGenderDialog.value = !_showGenderDialog.value
    }

    fun updateGender(gender: User.Gender) {
        viewModelScope.launch {
            val user = userRepository.getOrCreateDefault()
            userRepository.updateGender(user.id, gender)
            val prefs = KeystoreManager.getEncryptedSharedPreferences(context)
            prefs.edit().putString("b2p5q8r1", gender.value).apply()
            _showGenderDialog.value = false
            _genderChanged.trySend(Unit)
        }
    }

    fun updateDateOfBirth(dateOfBirth: String?) {
        viewModelScope.launch {
            val user = userRepository.getOrCreateDefault()
            userRepository.updateDateOfBirth(user.id, dateOfBirth)
        }
    }
}
