package com.tianshang.health.feature.fitness.domain

import com.tianshang.health.core.common.constants.CyclePhase
import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.repository.UserRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCycleFitnessRecommendationsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val periodRecordDao: PeriodRecordDao
) {
    suspend operator fun invoke(): CycleFitnessResult {
        val user = userRepository.getOrCreateDefault()
        if (user.gender == "male") return CycleFitnessResult.EMPTY

        val records = periodRecordDao.getByUserIdList(user.id).filter { !it.isDeleted }
        if (records.size < 2) return CycleFitnessResult.EMPTY

        val sorted = records.sortedByDescending { it.startDate }
        val lastRecord = sorted.first()
        val lastStart = LocalDate.parse(lastRecord.startDate)
        val prevRecord = sorted[1]
        val prevStart = LocalDate.parse(prevRecord.startDate)
        val cycleLength = ChronoUnit.DAYS.between(prevStart, lastStart).toInt()
            .coerceIn(HealthConstants.MIN_CYCLE_LENGTH, HealthConstants.MAX_CYCLE_LENGTH)

        val today = LocalDate.now()
        val phase = CyclePhase.fromDate(today, lastStart, cycleLength)
        val ovulationDay = (cycleLength - HealthConstants.DEFAULT_LUTEAL_PHASE_LENGTH)
            .coerceIn(HealthConstants.OVULATION_DAY_MIN, cycleLength - 1)

        val phaseDayRange = when (phase) {
            CyclePhase.MENSTRUAL -> "1-${HealthConstants.DEFAULT_PERIOD_LENGTH}"
            CyclePhase.FOLLICULAR -> "${HealthConstants.DEFAULT_PERIOD_LENGTH + 1}-${ovulationDay - 4}"
            CyclePhase.OVULATORY -> "${ovulationDay - 3}-${ovulationDay + 1}"
            CyclePhase.LUTEAL -> "${ovulationDay + 2}-$cycleLength"
        }

        val (recommended, avoid, adviceKey) = getPhaseRecommendations(phase)

        val result = CycleFitnessRecommendation(
            currentPhase = phase,
            phaseDayRange = phaseDayRange,
            recommendedExerciseTypes = recommended,
            avoidExerciseTypes = avoid,
            adviceKey = adviceKey
        )

        return CycleFitnessResult(
            recommendation = result,
            hasData = true
        )
    }

    private fun getPhaseRecommendations(phase: CyclePhase): Triple<List<String>, List<String>, String> {
        return when (phase) {
            CyclePhase.MENSTRUAL -> Triple(
                listOf("yoga", "walking", "pilates"),
                listOf("hiit", "running", "strength"),
                "menstrual"
            )

            CyclePhase.FOLLICULAR -> Triple(
                listOf("running", "strength", "cycling", "dance"),
                listOf("overtraining"),
                "follicular"
            )

            CyclePhase.OVULATORY -> Triple(
                listOf("hiit", "swimming", "tennis", "skipping"),
                listOf("overtraining"),
                "ovulatory"
            )

            CyclePhase.LUTEAL -> Triple(
                listOf("yoga", "walking", "pilates", "hiking"),
                listOf("hiit", "strength"),
                "luteal"
            )
        }
    }
}
