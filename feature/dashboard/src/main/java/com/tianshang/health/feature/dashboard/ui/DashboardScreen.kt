package com.tianshang.health.feature.dashboard.ui

import androidx.compose.foundation.clickable
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
import com.tianshang.health.feature.dashboard.ui.components.HealthInsightsCard
import com.tianshang.health.feature.dashboard.ui.components.PeriodStatusCard
import com.tianshang.health.feature.dashboard.ui.components.QuickActionsCard
import com.tianshang.health.feature.dashboard.ui.components.StepsOverviewCard
import com.tianshang.health.feature.dashboard.viewmodel.DashboardUiState
import com.tianshang.health.feature.dashboard.viewmodel.DashboardViewModel
import com.tianshang.health.feature.onboarding.model.Gender

@Composable
fun DashboardScreen(
    onNavigateToPeriod: () -> Unit = {},
    onNavigateToSteps: () -> Unit = {},
    onNavigateToHealthData: () -> Unit = {},
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToSleep: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is DashboardUiState.Loading -> {
            LoadingContent()
        }
        is DashboardUiState.Success -> {
            DashboardContent(
                state = state.state,
                onNavigateToPeriod = onNavigateToPeriod,
                onNavigateToSteps = onNavigateToSteps,
                onNavigateToHealthData = onNavigateToHealthData,
                onNavigateToNutrition = onNavigateToNutrition,
                onNavigateToSleep = onNavigateToSleep
            )
        }
        is DashboardUiState.Error -> {
            ErrorContent(
                message = state.message,
                onRetry = { viewModel.refresh() }
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: com.tianshang.health.feature.dashboard.viewmodel.DashboardState,
    onNavigateToPeriod: () -> Unit,
    onNavigateToSteps: () -> Unit,
    onNavigateToHealthData: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToSleep: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Steps Overview
        StepsOverviewCard(
            todaySteps = state.todaySteps,
            goal = state.stepsGoal,
            modifier = Modifier.clickable { onNavigateToSteps() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Period Status (only for female/other users)
        if (state.userGender != Gender.MALE) {
            PeriodStatusCard(
                currentCycleDay = state.currentCycleDay,
                isPeriodActive = state.isPeriodActive,
                modifier = Modifier.clickable { onNavigateToPeriod() }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Health Insights
        HealthInsightsCard(insights = state.insights)

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions
        Text(
            text = stringResource(R.string.dashboard_quick_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        QuickActionsCard(
            showPeriodAction = state.userGender != Gender.MALE,
            onRecordPeriod = onNavigateToPeriod,
            onRecordSteps = onNavigateToSteps,
            onRecordHealth = onNavigateToHealthData,
            onRecordNutrition = onNavigateToNutrition,
            onRecordSleep = onNavigateToSleep
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
                Text(stringResource(R.string.dashboard_retry))
            }
        }
    }
}
