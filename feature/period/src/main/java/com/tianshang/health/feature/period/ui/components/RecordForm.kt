package com.tianshang.health.feature.period.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tianshang.health.core.common.R
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordForm(
    selectedDate: LocalDate,
    onSave: (LocalDate, Int?, Int?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var flowLevel by remember { mutableIntStateOf(0) }
    var painLevel by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }

    val symptoms = listOf(
        stringResource(R.string.symptom_headache),
        stringResource(R.string.symptom_bloating),
        stringResource(R.string.symptom_breast_tenderness),
        stringResource(R.string.symptom_backache),
        stringResource(R.string.symptom_fatigue),
        stringResource(R.string.symptom_mood_swings),
        stringResource(R.string.symptom_acne),
        stringResource(R.string.symptom_increased_appetite),
        stringResource(R.string.symptom_insomnia),
        stringResource(R.string.symptom_nausea)
    )

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.record_for_date, selectedDate.toString()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Flow level
            Text(
                text = stringResource(R.string.flow_level),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FlowLevelChip(stringResource(R.string.flow_light), 1, flowLevel) { flowLevel = it }
                FlowLevelChip(stringResource(R.string.flow_medium), 2, flowLevel) { flowLevel = it }
                FlowLevelChip(stringResource(R.string.flow_heavy), 3, flowLevel) { flowLevel = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pain level
            Text(
                text = stringResource(R.string.pain_level),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PainLevelChip(stringResource(R.string.pain_none), 0, painLevel) { painLevel = it }
                PainLevelChip(stringResource(R.string.pain_mild), 1, painLevel) { painLevel = it }
                PainLevelChip(stringResource(R.string.pain_moderate), 2, painLevel) { painLevel = it }
                PainLevelChip(stringResource(R.string.pain_severe), 3, painLevel) { painLevel = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Symptoms
            Text(
                text = stringResource(R.string.symptoms),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                symptoms.forEach { symptom ->
                    FilterChip(
                        selected = selectedSymptoms.contains(symptom),
                        onClick = {
                            selectedSymptoms = if (selectedSymptoms.contains(symptom)) {
                                selectedSymptoms - symptom
                            } else {
                                selectedSymptoms + symptom
                            }
                        },
                        label = { Text(symptom) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    val notesText = if (notes.isNotBlank()) notes else null
                    onSave(
                        selectedDate,
                        if (flowLevel > 0) flowLevel else null,
                        if (painLevel > 0) painLevel else null,
                        notesText
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(stringResource(R.string.save_record))
            }
        }
    }
}

@Composable
private fun FlowLevelChip(
    label: String,
    level: Int,
    selectedLevel: Int,
    onSelect: (Int) -> Unit
) {
    FilterChip(
        selected = selectedLevel == level,
        onClick = { onSelect(level) },
        label = { Text(label) }
    )
}

@Composable
private fun PainLevelChip(
    label: String,
    level: Int,
    selectedLevel: Int,
    onSelect: (Int) -> Unit
) {
    FilterChip(
        selected = selectedLevel == level,
        onClick = { onSelect(level) },
        label = { Text(label) }
    )
}
