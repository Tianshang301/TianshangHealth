package com.tianshang.health.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import com.tianshang.health.core.database.entity.WorkoutRecord
import com.tianshang.health.feature.fitness.util.CalorieCalculator
import com.tianshang.health.feature.fitness.util.ExerciseType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun BodyMetricsCard(
    heightInput: String,
    weightInput: String,
    heightInputError: String? = null,
    weightInputError: String? = null,
    onHeightChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    val heightCm = heightInput.toFloatOrNull()
    val weightKg = weightInput.toFloatOrNull()
    val bmi = if (heightCm != null && weightKg != null && heightCm > 0f) {
        weightKg / ((heightCm / 100f) * (heightCm / 100f))
    } else {
        null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MonitorWeight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.body_metrics_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = onHeightChanged,
                        label = { Text(stringResource(R.string.body_metrics_height)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = heightInputError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (heightInputError != null) {
                        Text(
                            text = heightInputError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = onWeightChanged,
                        label = { Text(stringResource(R.string.body_metrics_weight)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = weightInputError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (weightInputError != null) {
                        Text(
                            text = weightInputError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
            }
            if (bmi != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.bmi_format, bmi),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
internal fun WorkoutItem(
    workout: WorkoutRecord,
    onDelete: () -> Unit
) {
    val exerciseType = ExerciseType.fromValue(workout.exerciseType)
    val dateFormat = stringResource(R.string.date_format_month_day)
    val dateFormatted = try {
        val date = LocalDate.parse(workout.date)
        date.format(DateTimeFormatter.ofPattern(dateFormat))
    } catch (_: Exception) {
        workout.date
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(exerciseType.displayNameResId),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = stringResource(R.string.fitness_minutes_format, workout.durationMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    val calories = workout.caloriesBurned
                    if (calories != null) {
                        val kcal = calories.toInt()
                        val kj = CalorieCalculator.kcalToKj(calories).toInt()
                        Text(
                            text = stringResource(R.string.workout_calories_format, kcal, kj),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                    val distance = workout.distanceMeters
                    if (distance != null) {
                        val km = distance / 1000f
                        Text(
                            text = " \u00B7 ${String.format("%.1f", km)}${stringResource(R.string.fitness_km_unit)}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
