package com.tianshang.health.feature.steps.domain

import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.repository.PeriodRecordRepository
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.feature.steps.data.repository.StepsRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class CycleStepsInsight(
    val follicularAvg: Float,
    val lutealAvg: Float
)

@Singleton
class GetCycleStepsInsightUseCase @Inject constructor(
    private val stepsRepository: StepsRepository,
    private val periodRecordRepository: PeriodRecordRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): CycleStepsInsight? {
        val user = userRepository.getOrCreateDefault()
        val records = periodRecordRepository.getByUserIdList(user.id)

        if (records.size < 2) return null

        val sortedRecords = records.sortedByDescending { it.startDate }
        val lastRecord = sortedRecords.first()
        val lastStartDate = LocalDate.parse(lastRecord.startDate)

        val cycleLength = if (sortedRecords.size >= 2) {
            val prevStart = LocalDate.parse(sortedRecords[1].startDate)
            ChronoUnit.DAYS.between(prevStart, lastStartDate).toInt()
        } else {
            HealthConstants.DEFAULT_CYCLE_LENGTH
        }

        val ovulationDay = cycleLength - HealthConstants.DEFAULT_LUTEAL_PHASE_LENGTH
        val follicularPhaseEnd = lastStartDate.plusDays(ovulationDay.toLong())
        val lutealPhaseStart = follicularPhaseEnd.plusDays(1)
        val today = LocalDate.now()

        val thirtyDaysAgo = today.minusDays(HealthConstants.RECENT_DAYS_MONTH.toLong())
        val steps = stepsRepository.getStepsByDateRangeSync(thirtyDaysAgo, today)

        if (steps.isEmpty()) return null

        val follicularSteps = steps.filter { step ->
            val stepDate = LocalDate.parse(step.date)
            !stepDate.isBefore(lastStartDate) && stepDate.isBefore(follicularPhaseEnd)
        }

        val lutealSteps = steps.filter { step ->
            val stepDate = LocalDate.parse(step.date)
            !stepDate.isBefore(lutealPhaseStart) && !stepDate.isAfter(today)
        }

        val follicularAvg = if (follicularSteps.isNotEmpty()) {
            follicularSteps.map { it.count }.average().toFloat()
        } else {
            0f
        }

        val lutealAvg = if (lutealSteps.isNotEmpty()) {
            lutealSteps.map { it.count }.average().toFloat()
        } else {
            0f
        }

        if (follicularAvg == 0f && lutealAvg == 0f) return null

        return CycleStepsInsight(
            follicularAvg = follicularAvg,
            lutealAvg = lutealAvg
        )
    }
}
