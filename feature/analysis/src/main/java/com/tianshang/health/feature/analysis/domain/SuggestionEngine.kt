package com.tianshang.health.feature.analysis.domain

import com.tianshang.health.core.common.R

object SuggestionEngine {

    private const val CALORIE_LOW = 1800f
    private const val CALORIE_HIGH = 2500f
    private const val PROTEIN_LOW = 50f
    private const val WATER_LOW = 1000f
    private const val SLEEP_LOW = 7f
    private const val SLEEP_HIGH = 9f
    private const val DEEP_SLEEP_RATIO_LOW = 0.15f
    private const val EXERCISE_MIN_WEEKLY = 150
    private const val SLEEP_QUALITY_LOW = 2.5f
    private const val STRESS_HIGH = 3.5f
    private const val MOOD_LOW = 2.5f

    fun generate(data: AnalysisData): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()

        data.nutrition?.let { n ->
            if (n.avgCalories > 0 && n.avgCalories < CALORIE_LOW) {
                suggestions.add(
                    Suggestion.Warning(
                        titleResId = R.string.suggestion_low_calorie_title,
                        descriptionResId = R.string.suggestion_low_calorie_desc,
                        formatArgs = listOf(n.avgCalories)
                    )
                )
            }
            if (n.avgCalories > CALORIE_HIGH) {
                suggestions.add(
                    Suggestion.Warning(
                        titleResId = R.string.suggestion_high_calorie_title,
                        descriptionResId = R.string.suggestion_high_calorie_desc,
                        formatArgs = listOf(n.avgCalories)
                    )
                )
            }
            if (n.avgProteinGrams > 0 && n.avgProteinGrams < PROTEIN_LOW) {
                suggestions.add(
                    Suggestion.Tip(
                        titleResId = R.string.suggestion_low_protein_title,
                        descriptionResId = R.string.suggestion_low_protein_desc,
                        formatArgs = listOf(n.avgProteinGrams)
                    )
                )
            }
            if (n.avgWaterMl > 0 && n.avgWaterMl < WATER_LOW) {
                suggestions.add(
                    Suggestion.Warning(
                        titleResId = R.string.suggestion_low_water_title,
                        descriptionResId = R.string.suggestion_low_water_desc,
                        formatArgs = listOf(n.avgWaterMl)
                    )
                )
            }
        }

        data.calorieBalance?.let { cb ->
            if (cb.avgCaloriesIn > 0 && cb.avgCaloriesBurned > 0) {
                val net = cb.avgCaloriesIn - cb.avgCaloriesBurned
                if (net > 300) {
                    suggestions.add(
                        Suggestion.Tip(
                            titleResId = R.string.suggestion_high_surplus_title,
                            descriptionResId = R.string.suggestion_high_surplus_desc,
                            formatArgs = listOf(net)
                        )
                    )
                } else if (net < -300) {
                    suggestions.add(
                        Suggestion.Tip(
                            titleResId = R.string.suggestion_high_deficit_title,
                            descriptionResId = R.string.suggestion_high_deficit_desc,
                            formatArgs = listOf(-net)
                        )
                    )
                } else if (kotlin.math.abs(net) < 100) {
                    suggestions.add(
                        Suggestion.Positive(
                            titleResId = R.string.suggestion_balanced_title,
                            descriptionResId = R.string.suggestion_balanced_desc
                        )
                    )
                }
            }
        }

        data.exercise?.let { e ->
            if (e.totalMinutes > 0 && e.totalMinutes < EXERCISE_MIN_WEEKLY) {
                suggestions.add(
                    Suggestion.Warning(
                        titleResId = R.string.suggestion_low_exercise_title,
                        descriptionResId = R.string.suggestion_low_exercise_desc,
                        formatArgs = listOf(e.totalMinutes)
                    )
                )
            } else if (e.totalMinutes >= EXERCISE_MIN_WEEKLY) {
                suggestions.add(
                    Suggestion.Positive(
                        titleResId = R.string.suggestion_exercise_met_title,
                        descriptionResId = R.string.suggestion_exercise_met_desc,
                        formatArgs = listOf(e.totalMinutes)
                    )
                )
            }
            if (e.typeDistribution.size <= 1 && e.totalMinutes > 0) {
                suggestions.add(
                    Suggestion.Tip(
                        titleResId = R.string.suggestion_exercise_variety_title,
                        descriptionResId = R.string.suggestion_exercise_variety_desc
                    )
                )
            }
        }

        data.sleep?.let { s ->
            if (s.avgHours > 0 && s.avgHours < SLEEP_LOW) {
                suggestions.add(
                    Suggestion.Warning(
                        titleResId = R.string.suggestion_low_sleep_title,
                        descriptionResId = R.string.suggestion_low_sleep_desc,
                        formatArgs = listOf(s.avgHours)
                    )
                )
            }
            if (s.avgHours > SLEEP_HIGH) {
                suggestions.add(
                    Suggestion.Tip(
                        titleResId = R.string.suggestion_high_sleep_title,
                        descriptionResId = R.string.suggestion_high_sleep_desc,
                        formatArgs = listOf(s.avgHours)
                    )
                )
            }
            if (s.avgHours > 0 && s.avgDeepHours > 0) {
                val ratio = s.avgDeepHours / s.avgHours
                if (ratio < DEEP_SLEEP_RATIO_LOW) {
                    suggestions.add(
                        Suggestion.Tip(
                            titleResId = R.string.suggestion_low_deep_sleep_title,
                            descriptionResId = R.string.suggestion_low_deep_sleep_desc,
                            formatArgs = listOf(ratio * 100)
                        )
                    )
                }
            }
            if (s.avgQuality > 0 && s.avgQuality <= SLEEP_QUALITY_LOW) {
                suggestions.add(
                    Suggestion.Warning(
                        titleResId = R.string.suggestion_low_sleep_quality_title,
                        descriptionResId = R.string.suggestion_low_sleep_quality_desc,
                        formatArgs = listOf(s.avgQuality)
                    )
                )
            }
        }

        if (data.sleep != null && data.phaseComparisons.isNotEmpty()) {
            val comparisons = data.phaseComparisons
            if (comparisons.size >= 2) {
                val sleepDrop = comparisons[0].sleepAvg - comparisons[1].sleepAvg
                if (sleepDrop > 0.5f) {
                    suggestions.add(
                        Suggestion.Tip(
                            titleResId = R.string.suggestion_luteal_sleep_title,
                            descriptionResId = R.string.suggestion_luteal_sleep_desc,
                            formatArgs = listOf(sleepDrop)
                        )
                    )
                }
            }
        }

        suggestions.sortBy { it.priority }
        return suggestions
    }
}
