package com.tianshang.health.feature.sleep.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BedWakeTimerCard(
    bedTime: Long?,
    wakeTime: Long?,
    onBedTimeClick: () -> Unit,
    onWakeTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasBoth = bedTime != null && wakeTime != null
    val hasOnlyBed = bedTime != null && wakeTime == null

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.sleep_record_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (hasBoth) {
                // Both timestamps recorded: show result
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
                // Only bedtime: show sleeping status
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
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.sleep_waketime_btn))
                }
            } else {
                // No timestamps: show "Go to Bed" button
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
    }
}
