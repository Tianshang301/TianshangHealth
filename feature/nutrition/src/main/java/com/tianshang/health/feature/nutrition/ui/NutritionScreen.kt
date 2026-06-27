package com.tianshang.health.feature.nutrition.ui

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
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.glass.EdgePosition
import com.tianshang.health.core.common.ui.glass.GlassCard
import com.tianshang.health.core.common.ui.glass.GlassFAB
import com.tianshang.health.core.common.ui.glass.GlassTopAppBar
import com.tianshang.health.core.common.ui.glass.GlassVariant
import com.tianshang.health.core.common.ui.glass.ScrollEdgeEffect
import com.tianshang.health.feature.nutrition.viewmodel.NutritionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val addState by viewModel.addState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (addState.saveSuccess) {
        showAddSheet = false
        viewModel.resetAddState()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = stringResource(R.string.nutrition_title)
            )
        },
        floatingActionButton = {
            GlassFAB(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.nutrition_add_meal))
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.loading))
            }
        } else if (state.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
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
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        DailyNutritionSummary(
                            state = state,
                            onAddWater = { ml -> viewModel.addWater(ml) }
                        )
                    }

                    if (state.cycleNutrition != null) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            CycleNutritionCard(
                                recommendation = state.cycleNutrition!!
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.nutrition_today_meals),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (state.todayMeals.isEmpty()) {
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                variant = GlassVariant.Regular,
                                cornerRadius = 28.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fastfood,
                                        contentDescription = null,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.nutrition_empty),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.todayMeals, key = { it.id }) { meal ->
                            MealItem(
                                meal = meal,
                                onDelete = { viewModel.deleteMeal(meal) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
                ScrollEdgeEffect(position = EdgePosition.Bottom)
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                viewModel.resetAddState()
            },
            sheetState = sheetState
        ) {
            AddMealContent(
                state = addState,
                onTypeSelected = { viewModel.updateMealType(it) },
                onFoodNameChanged = { viewModel.updateFoodName(it) },
                onCaloriesChanged = { viewModel.updateCalories(it) },
                onProteinChanged = { viewModel.updateProtein(it) },
                onCarbsChanged = { viewModel.updateCarbs(it) },
                onFatChanged = { viewModel.updateFat(it) },
                onServingSizeChanged = { viewModel.updateServingSize(it) },
                onNotesChanged = { viewModel.updateNotes(it) },
                onSave = { viewModel.saveMeal() },
                onDismiss = {
                    showAddSheet = false
                    viewModel.resetAddState()
                }
            )
        }
    }
}
