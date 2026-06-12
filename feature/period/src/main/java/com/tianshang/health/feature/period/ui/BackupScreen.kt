package com.tianshang.health.feature.period.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tianshang.health.core.common.R
import com.tianshang.health.feature.period.viewmodel.BackupUiState
import com.tianshang.health.feature.period.viewmodel.BackupViewModel

@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentAction by remember { mutableStateOf<BackupAction?>(null) }
    var password by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportBackup(it, password)
            password = ""
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importBackup(it, password)
            password = ""
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportCsv(it) }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is BackupUiState.Success -> {
                snackbarHostState.showSnackbar((uiState as BackupUiState.Success).message)
                viewModel.resetState()
            }
            is BackupUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as BackupUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.backup_restore_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.backup_aes_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            BackupCard(
                icon = Icons.Default.FileUpload,
                title = stringResource(R.string.export_backup),
                description = stringResource(R.string.backup_export_desc),
                actionLabel = stringResource(R.string.export_backup),
                isLoading = uiState is BackupUiState.Exporting,
                onClick = {
                    currentAction = BackupAction.EXPORT
                    password = ""
                    showPasswordDialog = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BackupCard(
                icon = Icons.Default.FileDownload,
                title = stringResource(R.string.import_backup),
                description = stringResource(R.string.backup_import_desc),
                actionLabel = stringResource(R.string.import_backup),
                isLoading = uiState is BackupUiState.Importing,
                onClick = {
                    currentAction = BackupAction.IMPORT
                    password = ""
                    showPasswordDialog = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BackupCard(
                icon = Icons.Default.Backup,
                title = stringResource(R.string.export_csv),
                description = stringResource(R.string.backup_csv_desc),
                actionLabel = stringResource(R.string.export_csv),
                isLoading = false,
                onClick = {
                    csvLauncher.launch("period_records.csv")
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                password = ""
            },
            title = {
                Text(
                    if (currentAction == BackupAction.EXPORT) {
                        stringResource(R.string.password_export_title)
                    } else {
                        stringResource(R.string.password_import_title)
                    }
                )
            },
            text = {
                Column {
                    Text(
                        if (currentAction == BackupAction.EXPORT) {
                            stringResource(R.string.password_export_desc)
                        } else {
                            stringResource(R.string.password_import_desc)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password_label)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        when (currentAction) {
                            BackupAction.EXPORT -> exportLauncher.launch("tianshang_backup.enc")
                            BackupAction.IMPORT -> importLauncher.launch(arrayOf("*/*"))
                            null -> {}
                        }
                    },
                    enabled = password.isNotBlank()
                ) {
                    Text(stringResource(R.string.proceed))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    password = ""
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private enum class BackupAction {
    EXPORT, IMPORT
}

@Composable
private fun BackupCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(onClick = onClick) {
                    Text(actionLabel)
                }
            }
        }
    }
}
