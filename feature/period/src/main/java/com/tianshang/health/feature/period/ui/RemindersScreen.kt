package com.tianshang.health.feature.period.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tianshang.health.core.common.R
import com.tianshang.health.feature.period.viewmodel.ReminderUiState
import com.tianshang.health.feature.period.viewmodel.ReminderViewModel

@Composable
fun RemindersScreen(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ReminderUiState.Loading -> {
            LoadingContent()
        }
        is ReminderUiState.Success -> {
            val settings = state.settings

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.reminder_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Period Reminder
                ReminderCard(
                    title = stringResource(R.string.period_reminder),
                    description = stringResource(R.string.reminder_period_desc),
                    enabled = settings.periodReminderEnabled,
                    onToggle = { viewModel.togglePeriodReminder(it) }
                ) {
                    if (settings.periodReminderEnabled) {
                        TimePickerRow(
                            label = stringResource(R.string.reminder_time),
                            hour = settings.periodReminderHour,
                            minute = settings.periodReminderMinute,
                            onTimeChange = { hour, minute ->
                                viewModel.setPeriodReminderTime(hour, minute)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ovulation Reminder
                ReminderCard(
                    title = stringResource(R.string.ovulation_reminder),
                    description = stringResource(R.string.reminder_ovulation_desc),
                    enabled = settings.ovulationReminderEnabled,
                    onToggle = { viewModel.toggleOvulationReminder(it) }
                ) {
                    if (settings.ovulationReminderEnabled) {
                        DaysBeforeRow(
                            label = stringResource(R.string.reminder_days_before),
                            value = settings.ovulationDaysBefore,
                            onValueChange = { viewModel.setOvulationDaysBefore(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // PMS Reminder
                ReminderCard(
                    title = stringResource(R.string.pms_reminder),
                    description = stringResource(R.string.reminder_pms_desc),
                    enabled = settings.pmsReminderEnabled,
                    onToggle = { viewModel.togglePmsReminder(it) }
                ) {
                    if (settings.pmsReminderEnabled) {
                        DaysBeforeRow(
                            label = stringResource(R.string.reminder_days_before),
                            value = settings.pmsDaysBefore,
                            onValueChange = { viewModel.setPmsDaysBefore(it) }
                        )
                    }
                }
            }
        }
        is ReminderUiState.Error -> {
            ErrorContent(
                message = state.message,
                onRetry = { viewModel.togglePeriodReminder(false) }
            )
        }
    }
}

@Composable
private fun ReminderCard(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun TimePickerRow(
    label: String,
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val newHour = (hour + 1) % 24
                    onTimeChange(newHour, minute)
                }
            ) {
                Text(String.format("%02d", hour))
            }

            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = {
                    val newMinute = (minute + 15) % 60
                    onTimeChange(hour, newMinute)
                }
            ) {
                Text(String.format("%02d", minute))
            }
        }
    }
}

@Composable
private fun DaysBeforeRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$value ${stringResource(R.string.unit_days)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..7f,
            steps = 5
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}
