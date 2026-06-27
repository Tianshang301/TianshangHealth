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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.glass.EdgePosition
import com.tianshang.health.core.common.ui.glass.GlassFAB
import com.tianshang.health.core.common.ui.glass.ScrollEdgeEffect
import com.tianshang.health.core.period.api.Confidence
import com.tianshang.health.feature.period.ui.components.CalendarView
import com.tianshang.health.feature.period.ui.components.CycleStatusCard
import com.tianshang.health.feature.period.ui.components.PredictionCard
import com.tianshang.health.feature.period.ui.components.RecordForm
import com.tianshang.health.feature.period.viewmodel.PeriodUiState
import com.tianshang.health.feature.period.viewmodel.PeriodViewModel

@Composable
fun PeriodScreen(
    onNavigateToReminders: () -> Unit = {},
    viewModel: PeriodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showRecordForm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            GlassFAB(
                onClick = { showRecordForm = !showRecordForm }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_record)
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is PeriodUiState.Loading -> {
                LoadingContent()
            }
            is PeriodUiState.Success -> {
                val periodState = state.periodState

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.period_tracking_title),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = MaterialTheme.typography.headlineMedium.fontWeight
                            )
                            IconButton(onClick = onNavigateToReminders) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = stringResource(R.string.reminder_title)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cycle status card
                        CycleStatusCard(
                            currentCycleDay = periodState.currentCycleDay,
                            daysUntilNextPeriod = periodState.daysUntilNextPeriod,
                            isPeriodActive = viewModel.isPeriodActive()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Calendar view
                        CalendarView(
                            selectedDate = selectedDate,
                            onDateSelected = { viewModel.selectDate(it) },
                            periodRecords = periodState.records,
                            ovulationDate = periodState.prediction?.ovulationDate,
                            fertileWindowStart = periodState.prediction?.fertileWindowStart,
                            fertileWindowEnd = periodState.prediction?.fertileWindowEnd
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Prediction card
                        if (periodState.prediction != null) {
                            PredictionCard(
                                nextPeriodDate = periodState.prediction.nextPeriodStart.toString(),
                                ovulationDate = periodState.prediction.ovulationDate.toString(),
                                fertileWindow = "${periodState.prediction.fertileWindowStart} - ${periodState.prediction.fertileWindowEnd}",
                                cycleLength = periodState.prediction.cycleLength,
                                confidence = when (periodState.prediction.confidence) {
                                    Confidence.HIGH -> stringResource(R.string.confidence_high)
                                    Confidence.MEDIUM -> stringResource(R.string.confidence_medium)
                                    Confidence.LOW -> stringResource(R.string.confidence_low)
                                    Confidence.INSUFFICIENT_DATA -> stringResource(R.string.confidence_insufficient)
                                },
                                cycleCount = periodState.records.size
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Record form
                        if (showRecordForm) {
                            RecordForm(
                                selectedDate = selectedDate,
                                onSave = { date, flowLevel, painLevel, notes ->
                                    viewModel.addPeriodRecord(date, null, flowLevel, painLevel, notes)
                                    showRecordForm = false
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Quick record button
                        Button(
                            onClick = { viewModel.startRecording() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (viewModel.isPeriodActive()) {
                                    stringResource(
                                        R.string.end_period
                                    )
                                } else {
                                    stringResource(R.string.start_period)
                                }
                            )
                        }
                    }
                    ScrollEdgeEffect(position = EdgePosition.Bottom)
                }
            }
            is PeriodUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.refresh() }
                )
            }
        }
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
