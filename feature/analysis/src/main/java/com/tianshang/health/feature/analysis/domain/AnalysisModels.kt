package com.tianshang.health.feature.analysis.domain

data class WeeklyNutrition(
    val avgCalories: Float,
    val avgProteinGrams: Float,
    val avgCarbsGrams: Float,
    val avgFatGrams: Float,
    val avgWaterMl: Float,
    val dailyCalories: List<DailyValue>,
    val dailyProtein: List<DailyValue>,
    val dailyCarbs: List<DailyValue>,
    val dailyFat: List<DailyValue>
)

data class WeeklySleep(
    val avgHours: Float,
    val avgDeepHours: Float,
    val avgQuality: Float,
    val dailyHours: List<DailyValue>,
    val dailyDeepHours: List<DailyValue>,
    val dailyQuality: List<DailyValue>
)

data class WeeklyExercise(
    val totalMinutes: Int,
    val avgMinutesPerDay: Float,
    val totalCaloriesBurned: Float,
    val dailyMinutes: List<DailyValue>,
    val typeDistribution: List<TypeValue>
)

data class CalorieBalance(
    val avgCaloriesIn: Float,
    val avgCaloriesBurned: Float,
    val avgRestingEnergy: Float,
    val avgTotalExpenditure: Float,
    val netDaily: List<Pair<String, Float>>
)

data class DailyValue(
    val label: String,
    val value: Float
)

data class TypeValue(
    val type: String,
    val value: Float
)

data class PhaseComparison(
    val phaseNameResId: Int,
    val sleepAvg: Float,
    val stepsAvg: Float,
    val moodAvg: Float,
    val stressAvg: Float,
    val calorieAvg: Float
)

sealed class Suggestion(
    open val titleResId: Int,
    open val descriptionResId: Int,
    open val priority: Int,
    open val formatArgs: List<Any> = emptyList()
) {
    data class Warning(
        override val titleResId: Int,
        override val descriptionResId: Int,
        override val priority: Int = 0,
        override val formatArgs: List<Any> = emptyList()
    ) : Suggestion(
        titleResId,
        descriptionResId,
        priority,
        formatArgs
    )
    data class Tip(
        override val titleResId: Int,
        override val descriptionResId: Int,
        override val priority: Int = 1,
        override val formatArgs: List<Any> = emptyList()
    ) : Suggestion(
        titleResId,
        descriptionResId,
        priority,
        formatArgs
    )
    data class Positive(
        override val titleResId: Int,
        override val descriptionResId: Int,
        override val priority: Int = 2,
        override val formatArgs: List<Any> = emptyList()
    ) : Suggestion(
        titleResId,
        descriptionResId,
        priority,
        formatArgs
    )
}

data class AnalysisData(
    val nutrition: WeeklyNutrition? = null,
    val sleep: WeeklySleep? = null,
    val exercise: WeeklyExercise? = null,
    val calorieBalance: CalorieBalance? = null,
    val phaseComparisons: List<PhaseComparison> = emptyList(),
    val suggestions: List<Suggestion> = emptyList(),
    val crossDimensionReport: CrossDimensionReport? = null
)

sealed class AnalysisUiState {
    data object Loading : AnalysisUiState()
    data class Success(val data: AnalysisData, val isFemale: Boolean) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

enum class CorrelationInterpretation {
    VERY_WEAK, WEAK, MODERATE, STRONG, VERY_STRONG
}

data class CorrelationPair(
    val dimensionA: String,
    val dimensionB: String,
    val correlation: Float,
    val sampleSize: Int,
    val interpretation: CorrelationInterpretation
)

data class CorrelationMatrix(
    val pairs: List<CorrelationPair>
)

data class HealthCompositeScore(
    val overallScore: Int,
    val sleepScore: Int,
    val nutritionScore: Int,
    val activityScore: Int,
    val moodScore: Int,
    val percentileVsSelf: Float
)

enum class PatternSeverity {
    INFO, WARNING, ALERT
}

data class CompoundPattern(
    val patternId: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val severity: PatternSeverity,
    val contributingDimensions: List<String>
)
