package com.tianshang.health.feature.fitness.domain

import com.tianshang.health.core.common.constants.CyclePhase

data class CycleFitnessRecommendation(
    val currentPhase: CyclePhase,
    val phaseDayRange: String,
    val recommendedExerciseTypes: List<String>,
    val avoidExerciseTypes: List<String>,
    val adviceKey: String
)

data class CycleFitnessResult(
    val recommendation: CycleFitnessRecommendation?,
    val hasData: Boolean
) {
    companion object {
        val EMPTY = CycleFitnessResult(null, false)
    }
}
