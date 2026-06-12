package com.tianshang.health.feature.period.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.tianshang.health.core.common.R
import com.tianshang.health.core.security.auth.BiometricAuthManager
import com.tianshang.health.feature.period.viewmodel.AppLockViewModel
import com.tianshang.health.feature.period.viewmodel.LockMode

@Composable
fun AppLockScreen(
    onAuthenticated: () -> Unit,
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
    initialMode: LockMode? = null,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = remember { context as FragmentActivity }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onAuthenticated()
        }
    }

    LaunchedEffect(initialMode) {
        if (initialMode != null) {
            viewModel.setInitialMode(initialMode)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.app_lock_health_data_protected),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        PinDots(
            length = state.enteredPin.length,
            maxLength = 6
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (state.mode) {
                LockMode.UNLOCK -> stringResource(R.string.app_lock_enter_password_title)
                LockMode.CREATE -> stringResource(R.string.app_lock_set_password_title)
                LockMode.CONFIRM -> stringResource(R.string.app_lock_confirm_password_label)
                LockMode.CHANGE_OLD -> stringResource(R.string.app_lock_current_pin)
                LockMode.CHANGE_NEW -> stringResource(R.string.app_lock_new_pin_label)
                LockMode.CHANGE_CONFIRM -> stringResource(R.string.app_lock_confirm_password_label)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (state.error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.error!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        PinKeypad(
            onDigit = viewModel::onDigit,
            onDelete = viewModel::onDelete,
            onConfirm = viewModel::onConfirm,
            showConfirm = state.enteredPin.length >= 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.mode == LockMode.UNLOCK) {
            val canDoBiometric = BiometricAuthManager.canAuthenticate(context)
            if (canDoBiometric) {
                IconButton(
                    onClick = {
                        BiometricAuthManager.showBiometricPrompt(
                            activity = activity,
                            title = context.getString(R.string.app_lock_use_biometric),
                            subtitle = context.getString(R.string.app_lock_health_data_protected),
                            onSuccess = {
                                viewModel.onUnlocked()
                            },
                            onError = { /* no-op */ },
                            onFailed = { /* no-op */ }
                        )
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = stringResource(R.string.app_lock_use_biometric),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        BiometricAuthManager.authenticateForRecovery(
                            activity = activity,
                            onSuccess = { viewModel.onRecoverySuccess() },
                            onError = { /* no-op */ }
                        )
                    }
                ) {
                    Text(
                        text = stringResource(R.string.app_lock_forgot_pin),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (state.mode == LockMode.CHANGE_OLD) {
            TextButton(
                onClick = onCancel
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PinDots(length: Int, maxLength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < length) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (index < length) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PinKeypad(
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
    showConfirm: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (row in listOf(listOf('1', '2', '3'), listOf('4', '5', '6'), listOf('7', '8', '9'))) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { digit ->
                    KeyButton(
                        text = digit.toString(),
                        onClick = { onDigit(digit) }
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .aspectRatio(1f)
            )
            KeyButton(
                text = "0",
                onClick = { onDigit('0') }
            )
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⌫",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        if (showConfirm) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onConfirm() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.confirm),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
