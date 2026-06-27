package com.tianshang.health.feature.sleep.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.glass.GlassCard
import com.tianshang.health.core.common.ui.glass.GlassVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BedWakeMode { TIMER, MANUAL }

@Composable
fun BedWakeTimerCard(
    mode: BedWakeMode,
    onModeToggle: () -> Unit,
    bedTime: Long?,
    wakeTime: Long?,
    onBedTimeClick: () -> Unit,
    onWakeTimeClick: () -> Unit,
    bedTimeStr: String = "",
    wakeTimeStr: String = "",
    sleepLatencyStr: String = "",
    wakeCountStr: String = "",
    onBedTimeStrChanged: (String) -> Unit = {},
    onWakeTimeStrChanged: (String) -> Unit = {},
    onSleepLatencyChanged: (String) -> Unit = {},
    onWakeCountChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    GlassCard(
        variant = GlassVariant.Regular,
        cornerRadius = 28.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sleep_schedule),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onModeToggle) {
                    Icon(
                        imageVector = if (mode == BedWakeMode.TIMER) Icons.Default.Edit else Icons.Default.Timer,
                        contentDescription = stringResource(
                            if (mode == BedWakeMode.TIMER) R.string.sleep_manual_input else R.string.sleep_timer_mode
                        ),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (mode == BedWakeMode.MANUAL) {
                ManualInput(
                    bedTimeStr = bedTimeStr,
                    wakeTimeStr = wakeTimeStr,
                    sleepLatencyStr = sleepLatencyStr,
                    wakeCountStr = wakeCountStr,
                    onBedTimeStrChanged = onBedTimeStrChanged,
                    onWakeTimeStrChanged = onWakeTimeStrChanged,
                    onSleepLatencyChanged = onSleepLatencyChanged,
                    onWakeCountChanged = onWakeCountChanged
                )
            } else {
                TimerInput(
                    bedTime = bedTime,
                    wakeTime = wakeTime,
                    onBedTimeClick = onBedTimeClick,
                    onWakeTimeClick = onWakeTimeClick
                )
            }
        }
    }
}

@Composable
private fun ManualInput(
    bedTimeStr: String,
    wakeTimeStr: String,
    sleepLatencyStr: String,
    wakeCountStr: String,
    onBedTimeStrChanged: (String) -> Unit,
    onWakeTimeStrChanged: (String) -> Unit,
    onSleepLatencyChanged: (String) -> Unit,
    onWakeCountChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = bedTimeStr,
            onValueChange = onBedTimeStrChanged,
            label = { Text(stringResource(R.string.sleep_bedtime_hint)) },
            placeholder = { Text("23:00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = wakeTimeStr,
            onValueChange = onWakeTimeStrChanged,
            label = { Text(stringResource(R.string.sleep_waketime_hint)) },
            placeholder = { Text("07:00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = sleepLatencyStr,
            onValueChange = onSleepLatencyChanged,
            label = { Text(stringResource(R.string.sleep_latency)) },
            placeholder = { Text("15") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = wakeCountStr,
            onValueChange = onWakeCountChanged,
            label = { Text(stringResource(R.string.sleep_wake_count)) },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TimerInput(
    bedTime: Long?,
    wakeTime: Long?,
    onBedTimeClick: () -> Unit,
    onWakeTimeClick: () -> Unit
) {
    val hasBoth = bedTime != null && wakeTime != null
    val hasOnlyBed = bedTime != null && wakeTime == null

    if (hasBoth) {
        val hours = (wakeTime!! - bedTime!!) / 3_600_000f
        Text(
            text = stringResource(R.string.sleep_auto_calculated, hours),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        Text(
            text = "${timeFormat.format(Date(bedTime))} → ${timeFormat.format(Date(wakeTime))}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onBedTimeClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(stringResource(R.string.sleep_bedtime_btn))
        }
    } else if (hasOnlyBed) {
        Icon(
            imageVector = Icons.Default.Bedtime,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.sleep_sleeping),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        val elapsedMs = System.currentTimeMillis() - bedTime!!
        val elapsedHours = elapsedMs / 3_600_000
        val elapsedMinutes = (elapsedMs % 3_600_000) / 60_000
        Text(
            text = stringResource(R.string.sleep_elapsed, elapsedHours.toInt(), elapsedMinutes.toInt()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onWakeTimeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.sleep_waketime_btn))
        }
    } else {
        Icon(
            imageVector = Icons.Default.Bedtime,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.sleep_bedtime_btn),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Button(
            onClick = onBedTimeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.sleep_bedtime_btn))
        }
    }
}
