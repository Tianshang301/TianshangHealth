package com.tianshang.health.feature.period.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.theme.FertileLightGreen
import com.tianshang.health.core.common.ui.theme.OvulationLightBlue
import com.tianshang.health.core.common.ui.theme.PeriodRed
import com.tianshang.health.core.database.entity.PeriodRecord
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    periodRecords: List<PeriodRecord>,
    ovulationDate: LocalDate?,
    fertileWindowStart: LocalDate?,
    fertileWindowEnd: LocalDate?,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    Column(modifier = modifier) {
        // Month header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Text(stringResource(R.string.calendar_prev))
            }

            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Text(stringResource(R.string.calendar_next))
            }
        }

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth()) {
            val weekdays = listOf(
                stringResource(R.string.week_sun),
                stringResource(R.string.week_mon),
                stringResource(R.string.week_tue),
                stringResource(R.string.week_wed),
                stringResource(R.string.week_thu),
                stringResource(R.string.week_fri),
                stringResource(R.string.week_sat)
            )
            weekdays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.atEndOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()

        var dayCounter = 1
        val rows = (daysInMonth + firstDayOfWeek + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    if (row == 0 && col < firstDayOfWeek || dayCounter > daysInMonth) {
                        // Empty cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        val date = currentMonth.atDay(dayCounter)
                        val isPeriod = isDateInPeriod(date, periodRecords)
                        val isOvulation = date == ovulationDate
                        val isFertile = fertileWindowStart != null && fertileWindowEnd != null &&
                            !date.isBefore(fertileWindowStart) && !date.isAfter(fertileWindowEnd)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()

                        CalendarDay(
                            date = date,
                            isPeriod = isPeriod,
                            isOvulation = isOvulation,
                            isFertile = isFertile,
                            isSelected = isSelected,
                            isToday = isToday,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )

                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isPeriod: Boolean,
    isOvulation: Boolean,
    isFertile: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isPeriod -> PeriodRed.copy(alpha = 0.3f)
        isOvulation -> OvulationLightBlue
        isFertile -> FertileLightGreen
        isToday -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isPeriod -> PeriodRed
        isOvulation -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun isDateInPeriod(date: LocalDate, records: List<PeriodRecord>): Boolean {
    return records.any { record ->
        val start = LocalDate.parse(record.startDate)
        val end = record.endDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
        !date.isBefore(start) && !date.isAfter(end)
    }
}
