package com.tianshang.health.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.glass.EdgePosition
import com.tianshang.health.core.common.ui.glass.GlassFAB
import com.tianshang.health.core.common.ui.glass.GlassTopAppBar
import com.tianshang.health.core.common.ui.glass.ScrollEdgeEffect
import com.tianshang.health.feature.fitness.ui.AddWorkoutContent
import com.tianshang.health.feature.fitness.ui.CycleFitnessRecommendationCard
import com.tianshang.health.feature.fitness.viewmodel.FitnessViewModel
import com.tianshang.health.feature.steps.ui.StepsCard
import com.tianshang.health.feature.steps.util.OemType
import com.tianshang.health.feature.steps.viewmodel.StepsUiState
import com.tianshang.health.feature.steps.viewmodel.StepsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    onNavigateToPeriodAnalysis: () -> Unit = {},
    stepsViewModel: StepsViewModel = hiltViewModel(),
    fitnessViewModel: FitnessViewModel = hiltViewModel()
) {
    val stepsUiState by stepsViewModel.uiState.collectAsState()
    val fitnessState by fitnessViewModel.state.collectAsState()
    val fitnessAddState by fitnessViewModel.addState.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(fitnessAddState.saveSuccess) {
        if (fitnessAddState.saveSuccess) {
            showAddSheet = false
            fitnessViewModel.resetAddState()
        }
    }

    LaunchedEffect(fitnessState.saveBodyMetricsSuccess) {
        if (fitnessState.saveBodyMetricsSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.body_metrics_saved))
            fitnessViewModel.clearBodyMetricsSuccess()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GlassTopAppBar(
                title = stringResource(R.string.nav_fitness)
            )
        },
        floatingActionButton = {
            GlassFAB(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.fitness_add_workout))
            }
        }
    ) { innerPadding ->
        when (stepsUiState) {
            is StepsUiState.Loading -> {
                if (fitnessState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is StepsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (stepsUiState as StepsUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { stepsViewModel.refresh() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            is StepsUiState.Success -> {
                val stepsState = (stepsUiState as StepsUiState.Success).stepsState

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
                        if (!stepsState.isBatteryOptimizationDisabled) {
                            item {
                                BatteryOptimizationCard(
                                    onDisable = { stepsViewModel.requestDisableBatteryOptimization() }
                                )
                            }
                        }

                        if (stepsState.oemType != OemType.OTHER) {
                            item {
                                OemGuideCard(
                                    oemType = stepsState.oemType,
                                    onOpenBatterySettings = { stepsViewModel.openOemBatterySettings() },
                                    onOpenAutoStartSettings = { stepsViewModel.openOemAutoStartSettings() }
                                )
                            }
                        }

                        item {
                            StepsCard(
                                todaySteps = stepsState.todaySteps,
                                todayGoal = stepsState.todayGoal,
                                weeklyAverage = stepsState.weeklyAverage,
                                onClick = onNavigateToPeriodAnalysis
                            )
                        }

                        item {
                            EnergySummaryCard(
                                todaySteps = stepsState.todaySteps,
                                todayStepsCalories = fitnessState.todayStepsCalories,
                                todayWorkoutCalories = fitnessState.totalCaloriesToday,
                                todayCombinedCalories = fitnessState.combinedDailyCalories,
                                weeklySteps = fitnessState.totalStepsThisWeek,
                                weeklyStepsCalories = fitnessState.weeklyStepsCalories,
                                weeklyWorkoutCalories = fitnessState.totalCaloriesThisWeek,
                                weeklyWorkoutCount = fitnessState.workoutCountThisWeek,
                                weeklyDurationMinutes = fitnessState.totalDurationThisWeek
                            )
                        }

                        if (fitnessState.cycleFitnessResult.hasData) {
                            item {
                                CycleFitnessRecommendationCard(
                                    recommendation = fitnessState.cycleFitnessResult.recommendation!!
                                )
                            }
                        }

                        item {
                            GoalAdjustmentCard(
                                todayGoal = stepsState.todayGoal,
                                onUpdateGoal = { stepsViewModel.updateGoal(it) }
                            )
                        }

                        if (stepsState.weeklySteps.isNotEmpty()) {
                            item {
                                WeeklyStepsChart(steps = stepsState.weeklySteps)
                            }
                        }

                        item {
                            BodyMetricsCard(
                                heightInput = fitnessState.heightInput,
                                weightInput = fitnessState.weightInput,
                                heightInputError = fitnessState.heightInputError,
                                weightInputError = fitnessState.weightInputError,
                                onHeightChanged = { fitnessViewModel.updateHeightInput(it) },
                                onWeightChanged = { fitnessViewModel.updateWeightInput(it) },
                                onSave = { fitnessViewModel.saveBodyMetrics() }
                            )
                        }

                        if (fitnessState.todayWorkouts.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.fitness_today_workouts),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(fitnessState.todayWorkouts, key = { it.id }) { workout ->
                                WorkoutItem(
                                    workout = workout,
                                    onDelete = { fitnessViewModel.deleteWorkout(workout) }
                                )
                            }
                        }

                        if (fitnessState.recentWorkouts.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.fitness_recent_workouts),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(fitnessState.recentWorkouts, key = { it.id }) { workout ->
                                WorkoutItem(
                                    workout = workout,
                                    onDelete = { fitnessViewModel.deleteWorkout(workout) }
                                )
                            }
                        }

                        if (fitnessState.todayWorkouts.isEmpty() && fitnessState.recentWorkouts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.fitness_empty),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                    ScrollEdgeEffect(position = EdgePosition.Bottom)
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState
        ) {
            AddWorkoutContent(
                state = fitnessAddState,
                onTypeSelected = { fitnessViewModel.updateSelectedType(it) },
                onDurationChanged = { fitnessViewModel.updateDuration(it) },
                onCaloriesChanged = { fitnessViewModel.updateCalories(it) },
                onDistanceChanged = { fitnessViewModel.updateDistance(it) },
                onNotesChanged = { fitnessViewModel.updateNotes(it) },
                onSave = { fitnessViewModel.saveWorkout() },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showAddSheet = false
                    }
                }
            )
        }
    }
}
