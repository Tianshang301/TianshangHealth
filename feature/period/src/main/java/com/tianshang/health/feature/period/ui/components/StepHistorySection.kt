package com.tianshang.health.feature.period.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.entity.DailySteps
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private enum class TimeTab { DAY, WEEK, MONTH, YEAR, ALL }

@Composable
fun StepHistorySection(
    allSteps: List<DailySteps>,
    stepsToKcal: (Int) -> Float
) {
    var selectedTab by remember { mutableStateOf(TimeTab.DAY) }

    // Build a date→count lookup for fast access
    val stepsByDate = remember(allSteps) {
        allSteps.associate { it.date to it.count }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.steps_history_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val labels = listOf(
                TimeTab.DAY to R.string.steps_tab_day,
                TimeTab.WEEK to R.string.steps_tab_week,
                TimeTab.MONTH to R.string.steps_tab_month,
                TimeTab.YEAR to R.string.steps_tab_year,
                TimeTab.ALL to R.string.steps_tab_all
            )
            labels.forEach { (tab, resId) ->
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    label = {
                        Text(
                            text = stringResource(resId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            TimeTab.DAY -> DayTab(stepsByDate, stepsToKcal)
            TimeTab.WEEK -> WeekTab(stepsByDate, stepsToKcal)
            TimeTab.MONTH -> MonthTab(stepsByDate, stepsToKcal)
            TimeTab.YEAR -> YearTab(stepsByDate, stepsToKcal)
            TimeTab.ALL -> AllTab(stepsByDate, stepsToKcal)
        }
    }
}

// ── Day Tab ─────────────────────────────────────────────────────────

@Composable
private fun DayTab(stepsByDate: Map<String, Int>, stepsToKcal: (Int) -> Float) {
    var offset by remember { mutableIntStateOf(0) } // 0 = today, -1 = yesterday, ...
    val date = LocalDate.now().plusDays(offset.toLong())
    val dateStr = date.toString()
    val steps = stepsByDate[dateStr] ?: 0
    val kcal = stepsToKcal(steps)
    val isToday = offset == 0

    val monthDayFormat = stringResource(R.string.date_format_month_day)
    NavigatorRow(
        label = if (isToday) {
            stringResource(R.string.steps_today)
        } else {
            date.format(DateTimeFormatter.ofPattern(monthDayFormat))
        },
        onPrev = { offset-- },
        onNext = { if (offset < 0) offset++ }
    )

    Spacer(modifier = Modifier.height(8.dp))

    SingleRecordCard(steps = steps, kcal = kcal)

    Spacer(modifier = Modifier.height(12.dp))

    // Chart: last 7 days trend (including selected day)
    val weekStart = date.minusDays(6)
    val weekData = (0..6).map { d ->
        val ds = weekStart.plusDays(d.toLong()).toString()
        ds to (stepsByDate[ds] ?: 0)
    }
    LabeledBarChart(
        label = stringResource(R.string.steps_daily_trend),
        data = weekData,
        labelFormatter = { it.takeLast(5) } // "MM-dd"
    )
}

// ── Week Tab ────────────────────────────────────────────────────────

@Composable
private fun WeekTab(stepsByDate: Map<String, Int>, stepsToKcal: (Int) -> Float) {
    var weekOffset by remember { mutableIntStateOf(0) } // 0 = current week
    val today = LocalDate.now()
    val baseMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val monday = baseMonday.plusWeeks(weekOffset.toLong())
    val sunday = monday.plusDays(6)

    val weekData = (0..6).map { d ->
        val ds = monday.plusDays(d.toLong()).toString()
        ds to (stepsByDate[ds] ?: 0)
    }
    val totalSteps = weekData.sumOf { it.second }
    val totalKcal = stepsToKcal(totalSteps)

    val fmt = DateTimeFormatter.ofPattern(stringResource(R.string.date_format_month_day))
    val label = "${monday.format(fmt)} - ${sunday.format(fmt)}"

    NavigatorRow(
        label = label,
        onPrev = { weekOffset-- },
        onNext = { if (weekOffset < 0) weekOffset++ }
    )

    Spacer(modifier = Modifier.height(8.dp))

    SingleRecordCard(steps = totalSteps, kcal = totalKcal)

    Spacer(modifier = Modifier.height(12.dp))

    LabeledBarChart(
        label = stringResource(R.string.steps_weekly_label),
        data = weekData,
        labelFormatter = { d ->
            LocalDate.parse(d).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    )
}

// ── Month Tab ───────────────────────────────────────────────────────

@Composable
private fun MonthTab(stepsByDate: Map<String, Int>, stepsToKcal: (Int) -> Float) {
    var monthOffset by remember { mutableIntStateOf(0) }
    val ym = YearMonth.now().plusMonths(monthOffset.toLong())
    val daysInMonth = ym.lengthOfMonth()

    val monthData = (1..daysInMonth).map { day ->
        val ds = ym.atDay(day).toString()
        ds to (stepsByDate[ds] ?: 0)
    }
    val totalSteps = monthData.sumOf { it.second }
    val totalKcal = stepsToKcal(totalSteps)

    NavigatorRow(
        label = ym.format(DateTimeFormatter.ofPattern("yyyy/MM")),
        onPrev = { monthOffset-- },
        onNext = { if (monthOffset < 0) monthOffset++ }
    )

    Spacer(modifier = Modifier.height(8.dp))

    SingleRecordCard(steps = totalSteps, kcal = totalKcal)

    Spacer(modifier = Modifier.height(12.dp))

    // Weekly breakdown within the month
    val weeklyData = monthData
        .groupBy { (dateStr, _) ->
            val d = LocalDate.parse(dateStr)
            val weekOfMonth = ((d.dayOfMonth - 1) / 7) + 1
            weekOfMonth
        }
        .map { (week, days) ->
            val weekStart = ym.atDay(((week - 1) * 7 + 1).coerceAtMost(daysInMonth))
            val weekEnd = ym.atDay((week * 7).coerceAtMost(daysInMonth))
            val fmt = DateTimeFormatter.ofPattern("d")
            "W$week(${weekStart.format(fmt)}-${weekEnd.format(fmt)})" to days.sumOf { it.second }
        }

    LabeledBarChart(
        label = stringResource(R.string.steps_weekly_label),
        data = weeklyData,
        labelFormatter = { it }
    )
}

// ── Year Tab ────────────────────────────────────────────────────────

@Composable
private fun YearTab(stepsByDate: Map<String, Int>, stepsToKcal: (Int) -> Float) {
    var yearOffset by remember { mutableIntStateOf(0) }
    val year = LocalDate.now().year + yearOffset

    val monthlyData = (1..12).map { month ->
        val ym = YearMonth.of(year, month)
        val total = (1..ym.lengthOfMonth()).sumOf { day ->
            stepsByDate[ym.atDay(day).toString()] ?: 0
        }
        ym.format(DateTimeFormatter.ofPattern("MM")) to total
    }
    val totalSteps = monthlyData.sumOf { it.second }
    val totalKcal = stepsToKcal(totalSteps)

    NavigatorRow(
        label = "$year",
        onPrev = { yearOffset-- },
        onNext = { if (yearOffset < 0) yearOffset++ }
    )

    Spacer(modifier = Modifier.height(8.dp))

    SingleRecordCard(steps = totalSteps, kcal = totalKcal)

    Spacer(modifier = Modifier.height(12.dp))

    LabeledBarChart(
        label = stringResource(R.string.steps_monthly_label),
        data = monthlyData,
        labelFormatter = { it }
    )
}

// ── All Tab ─────────────────────────────────────────────────────────

@Composable
private fun AllTab(stepsByDate: Map<String, Int>, stepsToKcal: (Int) -> Float) {
    val totalSteps = stepsByDate.values.sum()
    val totalKcal = stepsToKcal(totalSteps)

    SingleRecordCard(steps = totalSteps, kcal = totalKcal)

    Spacer(modifier = Modifier.height(12.dp))

    // Yearly breakdown
    val yearlyData = stepsByDate
        .entries
        .groupBy { it.key.substring(0, 4) } // "yyyy"
        .map { (year, entries) -> year to entries.sumOf { it.value } }
        .sortedBy { it.first }

    if (yearlyData.isNotEmpty()) {
        LabeledBarChart(
            label = stringResource(R.string.steps_yearly_label),
            data = yearlyData,
            labelFormatter = { it }
        )
    }
}

// ── Shared composables ──────────────────────────────────────────────

@Composable
private fun NavigatorRow(
    label: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.previous)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.next)
            )
        }
    }
}

@Composable
private fun SingleRecordCard(steps: Int, kcal: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(
                label = stringResource(R.string.steps_label),
                value = formatSteps(steps)
            )
            StatColumn(
                label = stringResource(R.string.steps_calories_label),
                value = stringResource(R.string.unit_kcal_format, kcal)
            )
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun <T> LabeledBarChart(
    label: String,
    data: List<Pair<T, Int>>,
    labelFormatter: (T) -> String
) {
    if (data.isEmpty()) return
    val maxVal = data.maxOf { it.second }.coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            data.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = labelFormatter(key),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(52.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val fraction = value.toFloat() / maxVal
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .padding(end = 6.dp),
                        color = if (value > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text(
                        text = formatSteps(value),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun formatSteps(steps: Int): String {
    val tenThousandsFormat = stringResource(R.string.number_ten_thousands_format)
    return if (steps >= HealthConstants.STEPS_FORMAT_TEN_THOUSANDS_THRESHOLD) {
        String.format(tenThousandsFormat, steps / HealthConstants.STEPS_FORMAT_TEN_THOUSANDS_THRESHOLD.toDouble())
    } else if (steps >= 1000) {
        String.format("%.1fk", steps / 1000.0)
    } else {
        "$steps"
    }
}
