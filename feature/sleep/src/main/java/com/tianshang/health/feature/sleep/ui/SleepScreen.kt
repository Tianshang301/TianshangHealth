package com.tianshang.health.feature.sleep.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.glass.EdgePosition
import com.tianshang.health.core.common.ui.glass.GlassCard
import com.tianshang.health.core.common.ui.glass.GlassTopAppBar
import com.tianshang.health.core.common.ui.glass.GlassVariant
import com.tianshang.health.core.common.ui.glass.ScrollEdgeEffect
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.feature.sleep.data.repository.SleepRepository.SleepConsistencyScore
import com.tianshang.health.feature.sleep.domain.HealthInsight
import com.tianshang.health.feature.sleep.domain.InsightType
import com.tianshang.health.feature.sleep.domain.SleepQualityIndex
import com.tianshang.health.feature.sleep.viewmodel.SleepViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: SleepViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var bedWakeMode by remember { mutableStateOf(BedWakeMode.MANUAL) }
    var timerBedTime by remember { mutableStateOf<Long?>(null) }
    var timerWakeTime by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = stringResource(R.string.sleep_title)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    SleepInputCard(
                        hoursInput = state.hoursInput,
                        deepHoursInput = state.deepHoursInput,
                        qualityInput = state.qualityInput,
                        onHoursChanged = viewModel::updateHours,
                        onDeepHoursChanged = viewModel::updateDeepHours,
                        onQualityChanged = viewModel::updateQuality,
                        onSave = viewModel::saveSleep,
                        isSaving = state.isSaving
                    )
                }

                item {
                    BedWakeSection(
                        mode = bedWakeMode,
                        onModeToggle = { bedWakeMode = if (bedWakeMode == BedWakeMode.TIMER) BedWakeMode.MANUAL else BedWakeMode.TIMER },
                        bedTimeStr = state.bedTimeInput,
                        wakeTimeStr = state.wakeTimeInput,
                        sleepLatencyStr = state.sleepLatencyInput,
                        wakeCountStr = state.wakeCountInput,
                        onBedTimeStrChanged = viewModel::updateBedTime,
                        onWakeTimeStrChanged = viewModel::updateWakeTime,
                        onSleepLatencyChanged = viewModel::updateSleepLatency,
                        onWakeCountChanged = viewModel::updateWakeCount,
                        timerBedTime = timerBedTime,
                        timerWakeTime = timerWakeTime,
                        onTimerBedTimeClick = { timerBedTime = System.currentTimeMillis() },
                        onTimerWakeTimeClick = {
                            timerWakeTime = System.currentTimeMillis()
                            timerBedTime?.let { bed ->
                                timerWakeTime?.let { wake ->
                                    val hours = (wake - bed) / 3_600_000f
                                    viewModel.updateHours("%.1f".format(hours))
                                }
                            }
                        }
                    )
                }

                if (state.error != null) {
                    item {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (state.todaySleep != null) {
                    item {
                        TodaySleepCard(sleep = state.todaySleep!!)
                    }
                }

                if (state.consistencyScore != null) {
                    item {
                        SleepConsistencyCard(score = state.consistencyScore!!)
                    }
                }

                if (state.qualityIndex != null) {
                    item {
                        SleepQualityGauge(index = state.qualityIndex!!)
                    }
                }

                if (state.insights.isNotEmpty()) {
                    item {
                        SleepInsightsCard(insights = state.insights)
                    }
                }

                if (state.recentSleep.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.sleep_history),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(state.recentSleep.sortedByDescending { it.date }, key = { it.id }) { day ->
                        SleepHistoryItem(sleep = day)
                    }
                } else if (!state.isLoading) {
                    item {
                        Text(
                            text = stringResource(R.string.sleep_no_data),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            ScrollEdgeEffect(position = EdgePosition.Bottom)
        }
    }
}

@Composable
private fun BedWakeSection(
    mode: BedWakeMode,
    onModeToggle: () -> Unit,
    bedTimeStr: String,
    wakeTimeStr: String,
    sleepLatencyStr: String,
    wakeCountStr: String,
    onBedTimeStrChanged: (String) -> Unit,
    onWakeTimeStrChanged: (String) -> Unit,
    onSleepLatencyChanged: (String) -> Unit,
    onWakeCountChanged: (String) -> Unit,
    timerBedTime: Long?,
    timerWakeTime: Long?,
    onTimerBedTimeClick: () -> Unit,
    onTimerWakeTimeClick: () -> Unit
) {
    BedWakeTimerCard(
        mode = mode,
        onModeToggle = onModeToggle,
        bedTime = timerBedTime,
        wakeTime = timerWakeTime,
        onBedTimeClick = onTimerBedTimeClick,
        onWakeTimeClick = onTimerWakeTimeClick,
        bedTimeStr = bedTimeStr,
        wakeTimeStr = wakeTimeStr,
        sleepLatencyStr = sleepLatencyStr,
        wakeCountStr = wakeCountStr,
        onBedTimeStrChanged = onBedTimeStrChanged,
        onWakeTimeStrChanged = onWakeTimeStrChanged,
        onSleepLatencyChanged = onSleepLatencyChanged,
        onWakeCountChanged = onWakeCountChanged
    )
}

@Composable
private fun SleepConsistencyCard(score: SleepConsistencyScore) {
    val color = when {
        score.overallScore >= 80f -> Color(0xFF4CAF50)
        score.overallScore >= 60f -> Color(0xFFFFC107)
        else -> Color(0xFFFF5252)
    }
    val labelRes = when {
        score.overallScore >= 80f -> R.string.sleep_consistency_excellent
        score.overallScore >= 60f -> R.string.sleep_consistency_fair
        else -> R.string.sleep_consistency_poor
    }

    GlassCard(
        variant = GlassVariant.Clear,
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sleep_consistency_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SubMetric(
                label = stringResource(R.string.sleep_consistency_bedtime),
                score = score.bedCV?.let {
                    (1f - it.coerceIn(0f, 1f)) * 100f
                } ?: 50f
            )
            Spacer(modifier = Modifier.height(4.dp))
            SubMetric(
                label = stringResource(R.string.sleep_consistency_waketime),
                score = score.wakeCV?.let {
                    (1f - it.coerceIn(0f, 1f)) * 100f
                } ?: 50f
            )
            Spacer(modifier = Modifier.height(4.dp))
            SubMetric(
                label = stringResource(R.string.sleep_consistency_duration),
                score = score.durationCV?.let {
                    (1f - it.coerceIn(0f, 1f)) * 100f
                } ?: 50f
            )
        }
    }
}

@Composable
private fun SubMetric(label: String, score: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = "%.0f".format(score), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = when {
                score >= 80f -> Color(0xFF4CAF50)
                score >= 60f -> Color(0xFFFFC107)
                else -> Color(0xFFFF5252)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun SleepInputCard(
    hoursInput: String,
    deepHoursInput: String,
    qualityInput: Int?,
    onHoursChanged: (String) -> Unit,
    onDeepHoursChanged: (String) -> Unit,
    onQualityChanged: (Int) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    GlassCard(
        variant = GlassVariant.Regular,
        cornerRadius = 28.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.sleep_record_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = hoursInput,
                    onValueChange = onHoursChanged,
                    label = { Text(stringResource(R.string.sleep_hours)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = deepHoursInput,
                    onValueChange = onDeepHoursChanged,
                    label = { Text(stringResource(R.string.sleep_deep_hours)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.sleep_quality),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..5) {
                    IconButton(
                        onClick = { onQualityChanged(i) },
                        modifier = Modifier.width(40.dp).height(40.dp)
                    ) {
                        Icon(
                            imageVector = if (qualityInput != null && i <= qualityInput) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = stringResource(R.string.sleep_quality_format, i),
                            tint = if (qualityInput != null && i <= qualityInput) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun TodaySleepCard(sleep: DailyHealth) {
    GlassCard(
        variant = GlassVariant.Regular,
        cornerRadius = 28.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.sleep_today_status),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val hours = sleep.sleepHours ?: 0f
                val deepHours = sleep.deepSleepHours ?: 0f
                val quality = sleep.sleepQuality ?: 0

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.sleep_hours),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "%.1f".format(hours),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.sleep_deep_hours),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "%.1f".format(deepHours),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.sleep_quality),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Row {
                        repeat(5) { i ->
                            Icon(
                                imageVector = if (i < quality) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.width(16.dp).height(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepQualityGauge(index: SleepQualityIndex) {
    val color = when {
        index.overall >= 80 -> Color(0xFF4CAF50)
        index.overall >= 60 -> Color(0xFFFFC107)
        else -> Color(0xFFFF5252)
    }
    val labelRes = when {
        index.overall >= 80 -> R.string.sleep_consistency_excellent
        index.overall >= 60 -> R.string.sleep_consistency_fair
        else -> R.string.sleep_consistency_poor
    }

    GlassCard(
        variant = GlassVariant.Clear,
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sleep_quality_index),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${index.overall}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            QualitySubBar(label = "Duration", score = index.durationAdequacy, weight = 40)
            QualitySubBar(label = "Regularity", score = index.regularity, weight = 25)
            QualitySubBar(label = "Deep Sleep", score = index.deepSleepRatio, weight = 20)
            QualitySubBar(label = "Continuity", score = index.continuity, weight = 15)
        }
    }
}

@Composable
private fun QualitySubBar(label: String, score: Int, weight: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label ($weight%)",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(120.dp)
        )
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.weight(1f).height(6.dp),
            color = when {
                score >= 80 -> Color(0xFF4CAF50)
                score >= 60 -> Color(0xFFFFC107)
                else -> Color(0xFFFF5252)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "$score",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp).padding(start = 4.dp)
        )
    }
}

@Composable
private fun SleepInsightsCard(insights: List<HealthInsight>) {
    GlassCard(
        variant = GlassVariant.Regular,
        cornerRadius = 28.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.sleep_insight_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            insights.forEach { insight ->
                val iconColor = when (insight.type) {
                    InsightType.POSITIVE -> Color(0xFF4CAF50)
                    InsightType.WARNING -> Color(0xFFFFC107)
                    InsightType.CRITICAL -> Color(0xFFFF5252)
                    InsightType.INFO -> MaterialTheme.colorScheme.primary
                }
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = when (insight.type) {
                            InsightType.POSITIVE -> "✓"
                            InsightType.WARNING -> "!"
                            InsightType.CRITICAL -> "✗"
                            InsightType.INFO -> "i"
                        },
                        color = iconColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insight.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SleepHistoryItem(sleep: DailyHealth) {
    val dateFormat = stringResource(R.string.date_format_month_day)
    val dateFormatted = try {
        val date = LocalDate.parse(sleep.date)
        date.format(DateTimeFormatter.ofPattern(dateFormat))
    } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
        sleep.date
    }
    val hours = sleep.sleepHours ?: 0f
    val deepHours = sleep.deepSleepHours ?: 0f
    val quality = sleep.sleepQuality ?: 0

    GlassCard(
        variant = GlassVariant.Regular,
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.sleep_hours_format, "%.1f".format(hours)),
                    style = MaterialTheme.typography.bodySmall
                )
                if (deepHours > 0f) {
                    Text(
                        text = stringResource(R.string.sleep_deep_hours_format, "%.1f".format(deepHours)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < quality) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
