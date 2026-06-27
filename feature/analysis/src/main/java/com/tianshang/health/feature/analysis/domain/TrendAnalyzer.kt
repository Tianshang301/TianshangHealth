package com.tianshang.health.feature.analysis.domain

import com.tianshang.health.core.database.entity.DailyHealth

data class DimensionTrend(
    val dimension: String,
    val direction: Direction,
    val magnitude: Float,
    val previousAvg: Float,
    val currentAvg: Float
) {
    enum class Direction { IMPROVING, DECLINING, STABLE }
}

data class CrossDimensionReport(
    val healthScore: HealthCompositeScore,
    val trends: List<DimensionTrend>,
    val patterns: List<CompoundPattern>,
    val correlations: CorrelationMatrix?
)

object TrendAnalyzer {

    fun computeTrends(
        currentRecords: List<DailyHealth>,
        previousRecords: List<DailyHealth>
    ): List<DimensionTrend> {
        val trends = mutableListOf<DimensionTrend>()

        if (currentRecords.isEmpty() || previousRecords.isEmpty()) return trends

        // Sleep hours (↑ = improving)
        val sleepCur = currentRecords.mapNotNull { it.sleepHours }.averageOrNull()
        val sleepPrev = previousRecords.mapNotNull { it.sleepHours }.averageOrNull()
        sleepCur?.let { cur ->
            sleepPrev?.let { prev ->
                if (prev > 0f) {
                    trends.add(makeTrend("sleep", cur, prev, higherIsBetter = true))
                }
            }
        }

        // Steps (↑ = improving)
        val stepsCur = currentRecords.mapNotNull { it.steps?.toFloat() }.averageOrNull()
        val stepsPrev = previousRecords.mapNotNull { it.steps?.toFloat() }.averageOrNull()
        stepsCur?.let { cur ->
            stepsPrev?.let { prev ->
                if (prev > 0f) {
                    trends.add(makeTrend("steps", cur, prev, higherIsBetter = true))
                }
            }
        }

        // Exercise minutes (↑ = improving)
        val exCur = currentRecords.mapNotNull { it.exerciseMinutes?.toFloat() }.averageOrNull()
        val exPrev = previousRecords.mapNotNull { it.exerciseMinutes?.toFloat() }.averageOrNull()
        exCur?.let { cur ->
            exPrev?.let { prev ->
                if (prev > 0f) {
                    trends.add(makeTrend("exercise", cur, prev, higherIsBetter = true))
                }
            }
        }

        // Mood (↑ = improving)
        val moodCur = currentRecords.mapNotNull { it.moodScore?.toFloat() }.averageOrNull()
        val moodPrev = previousRecords.mapNotNull { it.moodScore?.toFloat() }.averageOrNull()
        moodCur?.let { cur ->
            moodPrev?.let { prev ->
                if (prev > 0f) {
                    trends.add(makeTrend("mood", cur, prev, higherIsBetter = true))
                }
            }
        }

        // Stress (↓ = improving)
        val stressCur = currentRecords.mapNotNull { it.stressLevel?.toFloat() }.averageOrNull()
        val stressPrev = previousRecords.mapNotNull { it.stressLevel?.toFloat() }.averageOrNull()
        stressCur?.let { cur ->
            stressPrev?.let { prev ->
                if (prev > 0f) {
                    trends.add(makeTrend("stress", cur, prev, higherIsBetter = false))
                }
            }
        }

        // Water intake (↑ = improving)
        val waterCur = currentRecords.mapNotNull { it.waterIntake }.averageOrNull()
        val waterPrev = previousRecords.mapNotNull { it.waterIntake }.averageOrNull()
        waterCur?.let { cur ->
            waterPrev?.let { prev ->
                if (prev > 0f) {
                    trends.add(makeTrend("water", cur, prev, higherIsBetter = true))
                }
            }
        }

        // Calories intake — closer to 2000 is "better" (simplified)
        val calCur = currentRecords.mapNotNull { it.caloriesIntake }.averageOrNull()
        val calPrev = previousRecords.mapNotNull { it.caloriesIntake }.averageOrNull()
        calCur?.let { cur ->
            calPrev?.let { prev ->
                if (prev > 0f) {
                    trends.add(makeTrend("calories", cur, prev, higherIsBetter = null))
                }
            }
        }

        return trends
    }

    fun computeCrossDimensionReport(
        currentRecords: List<DailyHealth>,
        previousRecords: List<DailyHealth>,
        fullHistory: List<DailyHealth>
    ): CrossDimensionReport? {
        if (currentRecords.size < 7) return null

        val healthScore = CrossDimensionEngine.computeHealthCompositeScore(
            currentRecords = currentRecords,
            historicalRecords = fullHistory.ifEmpty { previousRecords }
        ) ?: return null

        val trends = computeTrends(currentRecords, previousRecords)
        val patterns = CrossDimensionEngine.detectCompoundPatterns(currentRecords)
        val correlations = CrossDimensionEngine.computeCorrelationMatrix(currentRecords)

        return CrossDimensionReport(
            healthScore = healthScore,
            trends = trends,
            patterns = patterns,
            correlations = correlations
        )
    }

    private fun makeTrend(
        dimension: String,
        currentAvg: Float,
        previousAvg: Float,
        higherIsBetter: Boolean?
    ): DimensionTrend {
        val change = if (previousAvg != 0f) (currentAvg - previousAvg) / previousAvg else 0f
        val direction = when {
            higherIsBetter == true -> if (change > 0.05f) {
                DimensionTrend.Direction.IMPROVING
            } else if (change < -0.05f) {
                DimensionTrend.Direction.DECLINING
            } else {
                DimensionTrend.Direction.STABLE
            }
            higherIsBetter == false ->
                if (change < -0.05f) {
                    DimensionTrend.Direction.IMPROVING
                } else if (change > 0.05f) {
                    DimensionTrend.Direction.DECLINING
                } else {
                    DimensionTrend.Direction.STABLE
                }
            else ->
                if (abs(change) < 0.05f) {
                    DimensionTrend.Direction.STABLE
                } else {
                    DimensionTrend.Direction.STABLE // neutral — no direction
                }
        }
        return DimensionTrend(
            dimension = dimension,
            direction = direction,
            magnitude = abs(change),
            previousAvg = previousAvg,
            currentAvg = currentAvg
        )
    }

    private fun List<Float>.averageOrNull(): Float? {
        if (isEmpty()) return null
        return sum() / size
    }

    private fun List<Double>.averageOrNull(): Double? {
        if (isEmpty()) return null
        return sum() / size
    }

    private fun abs(v: Float): Float = if (v < 0f) -v else v
}
