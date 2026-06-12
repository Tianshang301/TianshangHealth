package com.tianshang.health.feature.period.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.tianshang.health.core.database.entity.User
import com.tianshang.health.feature.period.ui.components.CycleLengthChart
import com.tianshang.health.feature.period.ui.components.PainTrendChart
import com.tianshang.health.feature.period.ui.components.StatisticsSummary
import com.tianshang.health.feature.period.ui.components.StepHistorySection
import com.tianshang.health.feature.period.viewmodel.PeriodUiState
import com.tianshang.health.feature.period.viewmodel.PeriodViewModel

@Composable
fun AnalysisScreen(
    viewModel: PeriodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userGender by viewModel.userGender.collectAsState()
    val allSteps by viewModel.allSteps.collectAsState()

    when (val state = uiState) {
        is PeriodUiState.Loading -> {
            LoadingContent()
        }
        is PeriodUiState.Success -> {
            val periodState = state.periodState

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.analysis_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (userGender != User.Gender.MALE) {
                    // Statistics Summary
                    StatisticsSummary(records = periodState.records)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cycle Length Chart
                    CycleLengthChart(records = periodState.records)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pain Trend Chart
                    PainTrendChart(records = periodState.records)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prediction explanation
                    if (periodState.prediction != null) {
                        Text(
                            text = stringResource(R.string.prediction_explanation_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = periodState.prediction.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Historical step statistics (for all genders)
                Spacer(modifier = Modifier.height(24.dp))
                StepHistorySection(
                    allSteps = allSteps,
                    stepsToKcal = { steps -> viewModel.stepsToKcal(steps) }
                )
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
