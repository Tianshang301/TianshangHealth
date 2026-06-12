package com.tianshang.health.feature.dashboard.domain

import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.DailySymptomDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.repository.UserRepository
import com.tianshang.health.feature.onboarding.model.Gender
import com.tianshang.health.feature.steps.domain.GetCycleStepsInsightUseCase
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetHealthInsightsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val periodRecordDao: PeriodRecordDao,
    private val dailyHealthDao: DailyHealthDao,
    private val dailySymptomDao: DailySymptomDao,
    private val getCycleStepsInsight: GetCycleStepsInsightUseCase
) {
    suspend operator fun invoke(userGender: Gender): InsightResult {
        val user = userRepository.getOrCreateDefault()
        val records = periodRecordDao.getByUserIdList(user.id).filter { !it.isDeleted }
        val today = LocalDate.now()
        val dataList = mutableListOf<InsightData>()
        var hasAnyData = false

        val phaseStats = computePhaseStats(user.id, records, today)

        if (phaseStats != null) {
            hasAnyData = true
            hasAnyData = addStepsInsight(dataList, phaseStats) || hasAnyData
            hasAnyData = addMoodInsight(dataList, phaseStats) || hasAnyData
            hasAnyData = addSleepInsight(dataList, phaseStats) || hasAnyData
            hasAnyData = addStressInsight(dataList, phaseStats) || hasAnyData
            hasAnyData = addNutritionInsight(dataList, phaseStats) || hasAnyData
        }

        if (userGender != Gender.MALE && records.size >= 2) {
            val cycleStepsResult = getCycleStepsInsight()
            if (cycleStepsResult != null) {
                hasAnyData = true
                dataList.add(InsightData.CycleStepsResult(cycleStepsResult.follicularAvg, cycleStepsResult.lutealAvg))
            }
        }

        val todayHealth = dailyHealthDao.getTodayData(user.id, today.toString())
        if (todayHealth != null) {
            hasAnyData = true
            addTodayTips(dataList, todayHealth)
        }

        return InsightResult(
            data = dataList,
            hasData = hasAnyData
        )
    }

    private suspend fun computePhaseStats(userId: Long, records: List<com.tianshang.health.core.database.entity.PeriodRecord>, today: LocalDate): PhaseStats? {
        if (records.isEmpty()) return null
        val sorted = records.sortedByDescending { it.startDate }
        val lastRecord = sorted.first()
        val lastStart = LocalDate.parse(lastRecord.startDate)
        val cycleLength = if (sorted.size >= 2) {
            ChronoUnit.DAYS.between(LocalDate.parse(sorted[1].startDate), lastStart).toInt()
        } else {
            HealthConstants.DEFAULT_CYCLE_LENGTH
        }.coerceIn(HealthConstants.MIN_CYCLE_LENGTH, HealthConstants.MAX_CYCLE_LENGTH)

        val ovulationDay = (cycleLength - HealthConstants.DEFAULT_LUTEAL_PHASE_LENGTH)
            .coerceAtLeast(HealthConstants.OVULATION_DAY_MIN)
        val follicularEnd = lastStart.plusDays(ovulationDay.toLong())
        val lutealStart = follicularEnd.plusDays(1)

        val thirtyDaysAgo = today.minusDays(HealthConstants.RECENT_DAYS_MONTH.toLong())

        val follicularHealth = dailyHealthDao.getByDateRange(userId, thirtyDaysAgo.toString(), follicularEnd.toString())
        val lutealHealth = dailyHealthDao.getByDateRange(userId, lutealStart.toString(), today.toString())

        fun avgSteps(list: List<DailyHealth>) = list.mapNotNull {
            it.steps?.toFloat()
        }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
        fun avgMood(
            list: List<DailyHealth>
        ) = list.mapNotNull { it.moodScore?.toFloat() }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
        fun avgSleep(
            list: List<DailyHealth>
        ) = list.mapNotNull { it.sleepQuality?.toFloat() }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
        fun avgStress(
            list: List<DailyHealth>
        ) = list.mapNotNull { it.stressLevel?.toFloat() }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
        fun avgCalories(
            list: List<DailyHealth>
        ) = list.mapNotNull { it.caloriesIntake?.toFloat() }.takeIf { it.isNotEmpty() }?.average()?.toFloat()

        val lastEnd = lastRecord.endDate?.let { LocalDate.parse(it) }
        val periodHealth = if (lastEnd != null) {
            dailyHealthDao.getByDateRange(userId, lastStart.toString(), lastEnd.toString())
        } else {
            dailyHealthDao.getByDateRange(userId, lastStart.toString(), lastStart.toString())
        }

        return PhaseStats(
            follicularAvgSteps = avgSteps(follicularHealth),
            lutealAvgSteps = avgSteps(lutealHealth),
            follicularAvgMood = avgMood(follicularHealth),
            lutealAvgMood = avgMood(lutealHealth),
            follicularAvgSleep = avgSleep(follicularHealth),
            lutealAvgSleep = avgSleep(lutealHealth),
            follicularAvgStress = avgStress(follicularHealth),
            lutealAvgStress = avgStress(lutealHealth),
            periodMoodAvg = avgMood(periodHealth),
            periodSleepAvg = avgSleep(periodHealth),
            periodStressAvg = avgStress(periodHealth)
        )
    }

    private fun addStepsInsight(dataList: MutableList<InsightData>, stats: PhaseStats): Boolean {
        val f = stats.follicularAvgSteps ?: return false
        val l = stats.lutealAvgSteps ?: return false
        if (f == 0f && l == 0f) return false
        dataList.add(InsightData.StepsActivity(f, l))
        return true
    }

    private fun addMoodInsight(dataList: MutableList<InsightData>, stats: PhaseStats): Boolean {
        val f = stats.follicularAvgMood ?: return false
        val l = stats.lutealAvgMood ?: return false
        if (f == 0f && l == 0f) return false
        dataList.add(InsightData.MoodChange(f, l))
        return true
    }

    private fun addSleepInsight(dataList: MutableList<InsightData>, stats: PhaseStats): Boolean {
        val f = stats.follicularAvgSleep ?: return false
        val l = stats.lutealAvgSleep ?: return false
        if (f == 0f && l == 0f) return false
        dataList.add(InsightData.SleepChange(f, l))
        return true
    }

    private fun addStressInsight(dataList: MutableList<InsightData>, stats: PhaseStats): Boolean {
        val p = stats.periodStressAvg ?: return false
        val f = stats.follicularAvgStress ?: return false
        if (p == 0f && f == 0f) return false
        dataList.add(InsightData.StressChange(p, f))
        return true
    }

    private fun addNutritionInsight(dataList: MutableList<InsightData>, stats: PhaseStats): Boolean {
        val f = stats.follicularAvgCalories ?: return false
        val l = stats.lutealAvgCalories ?: return false
        if (f == 0f && l == 0f) return false
        dataList.add(InsightData.NutritionChange(f, l))
        return true
    }

    private fun addTodayTips(dataList: MutableList<InsightData>, today: DailyHealth) {
        today.moodScore?.let { score ->
            if (score <= 2) dataList.add(InsightData.TodayMood(score))
        }
        today.stressLevel?.let { level ->
            if (level >= 4) dataList.add(InsightData.TodayStress(level))
        }
        today.sleepQuality?.let { quality ->
            if (quality <= 2) dataList.add(InsightData.TodaySleep(quality))
        }
    }
}

data class InsightResult(
    val data: List<InsightData>,
    val hasData: Boolean
) {
    companion object {
        val EMPTY = InsightResult(emptyList(), false)
    }
}
