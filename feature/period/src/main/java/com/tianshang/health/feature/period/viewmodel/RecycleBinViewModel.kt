package com.tianshang.health.feature.period.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.database.repository.PeriodRecordRepository
import com.tianshang.health.core.database.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RecycleBinUiState {
    object Loading : RecycleBinUiState()
    data class Success(val records: List<PeriodRecord>) : RecycleBinUiState()
    data class Error(val message: String) : RecycleBinUiState()
}

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val periodRecordRepository: PeriodRecordRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecycleBinUiState>(RecycleBinUiState.Loading)
    val uiState: StateFlow<RecycleBinUiState> = _uiState.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var currentUserId: Long = 0

    init {
        loadDeletedRecords()
    }

    private fun loadDeletedRecords() {
        viewModelScope.launch {
            try {
                _uiState.value = RecycleBinUiState.Loading
                val user = userRepository.getOrCreateDefault()
                currentUserId = user.id
                val records = periodRecordRepository.getDeletedRecordsList(currentUserId)
                _uiState.value = RecycleBinUiState.Success(records)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = RecycleBinUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun restore(recordId: Long) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                periodRecordRepository.restore(recordId)
                loadDeletedRecords()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = RecycleBinUiState.Error(e.message ?: "Failed to restore")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun permanentlyDelete(recordId: Long) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                periodRecordRepository.deleteById(recordId)
                loadDeletedRecords()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = RecycleBinUiState.Error(e.message ?: "Failed to delete")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun emptyBin() {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                val records = periodRecordRepository.getDeletedRecordsList(currentUserId)
                records.forEach { periodRecordRepository.deleteById(it.id) }
                loadDeletedRecords()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                _uiState.value = RecycleBinUiState.Error(e.message ?: "Failed to empty bin")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun refresh() {
        loadDeletedRecords()
    }
}
