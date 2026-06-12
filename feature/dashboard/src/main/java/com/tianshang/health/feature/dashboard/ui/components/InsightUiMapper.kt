package com.tianshang.health.feature.dashboard.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Mood
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.feature.dashboard.domain.InsightCategory
import com.tianshang.health.feature.dashboard.domain.InsightData
import com.tianshang.health.feature.dashboard.domain.InsightItem

@Composable
fun mapInsightDataToItems(data: List<InsightData>): List<InsightItem> {
    val items = mutableListOf<InsightItem>()
    for (insight in data) {
        val item = insight.toInsightItem()
        if (item != null) items.add(item)
    }
    return items
}

@Composable
private fun InsightData.toInsightItem(): InsightItem? {
    return when (this) {
        is InsightData.StepsActivity -> {
            if (follicularAvg == 0f && lutealAvg == 0f) return null
            val ratio = if (follicularAvg > 0f) lutealAvg / follicularAvg else 1f
            when {
                ratio < HealthConstants.LUTEAL_ACTIVITY_LOW_RATIO_THRESHOLD -> InsightItem(
                    icon = Icons.Default.DirectionsWalk,
                    title = stringResource(R.string.insight_luteal_activity_low_title),
                    description = stringResource(
                        R.string.insight_luteal_activity_low_desc,
                        ((1 - ratio) * 100).toInt()
                    ),
                    category = InsightCategory.WARNING
                )
                ratio > 1.3f -> InsightItem(
                    icon = Icons.Default.DirectionsWalk,
                    title = stringResource(R.string.insight_luteal_activity_high_title),
                    description = stringResource(
                        R.string.insight_luteal_activity_high_desc,
                        ((ratio - 1) * 100).toInt()
                    ),
                    category = InsightCategory.INFO
                )
                else -> InsightItem(
                    icon = Icons.Default.DirectionsWalk,
                    title = stringResource(R.string.insight_activity_stable_title),
                    description = stringResource(R.string.insight_activity_stable_desc),
                    category = InsightCategory.POSITIVE
                )
            }
        }

        is InsightData.MoodChange -> {
            if (follicularAvg == 0f && lutealAvg == 0f) return null
            val diff = lutealAvg - follicularAvg
            when {
                diff < -0.5f -> InsightItem(
                    icon = Icons.Default.Mood,
                    title = stringResource(R.string.insight_mood_luteal_drop_title),
                    description = stringResource(R.string.insight_mood_luteal_drop_desc, "%.1f".format(-diff)),
                    category = InsightCategory.WARNING
                )
                diff > 0.5f -> InsightItem(
                    icon = Icons.Default.Mood,
                    title = stringResource(R.string.insight_mood_luteal_rise_title),
                    description = stringResource(R.string.insight_mood_luteal_rise_desc, "%.1f".format(diff)),
                    category = InsightCategory.POSITIVE
                )
                else -> null
            }
        }

        is InsightData.SleepChange -> {
            if (follicularAvg == 0f && lutealAvg == 0f) return null
            val diff = lutealAvg - follicularAvg
            when {
                diff < -0.5f -> InsightItem(
                    icon = Icons.Default.Hotel,
                    title = stringResource(R.string.insight_sleep_luteal_drop_title),
                    description = stringResource(R.string.insight_sleep_luteal_drop_desc, "%.1f".format(-diff)),
                    category = InsightCategory.WARNING
                )
                diff > 0.5f -> InsightItem(
                    icon = Icons.Default.Hotel,
                    title = stringResource(R.string.insight_sleep_luteal_rise_title),
                    description = stringResource(R.string.insight_sleep_luteal_rise_desc, "%.1f".format(diff)),
                    category = InsightCategory.POSITIVE
                )
                else -> null
            }
        }

        is InsightData.StressChange -> {
            val diff = periodAvg - follicularAvg
            if (diff > 0.5f) {
                InsightItem(
                    icon = Icons.Default.Mood,
                    title = stringResource(R.string.insight_stress_period_title),
                    description = stringResource(R.string.insight_stress_period_desc, "%.1f".format(diff)),
                    category = InsightCategory.WARNING
                )
            } else {
                null
            }
        }

        is InsightData.CycleStepsResult -> {
            when {
                lutealAvg < follicularAvg * 0.85f -> InsightItem(
                    icon = Icons.Default.DirectionsWalk,
                    title = stringResource(R.string.insight_luteal_decrease),
                    description = stringResource(R.string.insight_luteal_rest),
                    category = InsightCategory.WARNING
                )
                lutealAvg > follicularAvg * 1.15f -> InsightItem(
                    icon = Icons.Default.DirectionsWalk,
                    title = stringResource(R.string.insight_luteal_increase),
                    description = stringResource(R.string.insight_luteal_nutrition),
                    category = InsightCategory.INFO
                )
                else -> null
            }
        }

        is InsightData.NutritionChange -> {
            if (follicularAvgCalories == 0f && lutealAvgCalories == 0f) return null
            val diff = lutealAvgCalories - follicularAvgCalories
            when {
                diff < -100f -> InsightItem(
                    icon = Icons.Default.Favorite,
                    title = stringResource(R.string.insight_nutrition_luteal_low_title),
                    description = stringResource(R.string.insight_nutrition_luteal_low_desc, "%.0f".format(-diff)),
                    category = InsightCategory.WARNING
                )
                diff > 100f -> InsightItem(
                    icon = Icons.Default.Favorite,
                    title = stringResource(R.string.insight_nutrition_luteal_high_title),
                    description = stringResource(R.string.insight_nutrition_luteal_high_desc, "%.0f".format(diff)),
                    category = InsightCategory.POSITIVE
                )
                else -> InsightItem(
                    icon = Icons.Default.Favorite,
                    title = stringResource(R.string.insight_nutrition_stable_title),
                    description = stringResource(R.string.insight_nutrition_stable_desc),
                    category = InsightCategory.INFO
                )
            }
        }

        is InsightData.TodayMood -> {
            InsightItem(
                icon = Icons.Default.Mood,
                title = stringResource(R.string.insight_mood_today_low_title),
                description = stringResource(R.string.insight_mood_today_low_desc),
                category = InsightCategory.WARNING
            )
        }

        is InsightData.TodayStress -> {
            InsightItem(
                icon = Icons.Default.Mood,
                title = stringResource(R.string.insight_stress_today_high_title),
                description = stringResource(R.string.insight_stress_today_high_desc),
                category = InsightCategory.WARNING
            )
        }

        is InsightData.TodaySleep -> {
            InsightItem(
                icon = Icons.Default.Hotel,
                title = stringResource(R.string.insight_sleep_last_night_low_title),
                description = stringResource(R.string.insight_sleep_last_night_low_desc),
                category = InsightCategory.WARNING
            )
        }
    }
}
