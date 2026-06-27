package com.tianshang.health.feature.period.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.util.StringResolver
import com.tianshang.health.feature.period.backup.EncryptedBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.crypto.AEADBadTagException
import javax.inject.Inject

sealed class BackupUiState {
    object Idle : BackupUiState()
    object Exporting : BackupUiState()
    object Importing : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stringResolver: StringResolver,
    private val backupManager: EncryptedBackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = BackupUiState.Exporting
                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IllegalStateException("Cannot open file for writing")
                backupManager.exportBackup(password, outputStream)
                _uiState.value = BackupUiState.Success(stringResolver.getString(R.string.backup_exported_success))
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
                _uiState.value = BackupUiState.Error(stringResolver.getString(R.string.backup_fail))
            }
        }
    }

    fun importBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = BackupUiState.Importing
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Cannot open file for reading")
                val count = backupManager.importBackup(password, inputStream)
                _uiState.value = BackupUiState.Success(stringResolver.getString(R.string.import_success_format, count))
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: AEADBadTagException) {
                _uiState.value = BackupUiState.Error(stringResolver.getString(R.string.backup_wrong_password))
            } catch (_: IllegalArgumentException) {
                _uiState.value = BackupUiState.Error(stringResolver.getString(R.string.backup_invalid_file))
            } catch (_: Exception) {
                _uiState.value = BackupUiState.Error(stringResolver.getString(R.string.restore_fail))
            }
        }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = BackupUiState.Exporting
                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IllegalStateException("Cannot open file for writing")

                val csv = buildString {
                    appendLine(stringResolver.getString(R.string.csv_header))
                }

                outputStream.use { out ->
                    out.write(csv.toByteArray(Charsets.UTF_8))
                    out.flush()
                }

                _uiState.value = BackupUiState.Success(stringResolver.getString(R.string.csv_exported_success))
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
                _uiState.value = BackupUiState.Error(stringResolver.getString(R.string.csv_export_failed))
            }
        }
    }

    fun resetState() {
        _uiState.value = BackupUiState.Idle
    }
}
