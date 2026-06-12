package com.tianshang.health.core.common.constants

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class CyclePhase {
    MENSTRUAL,
    FOLLICULAR,
    OVULATORY,
    LUTEAL;

    companion object {
        fun fromDate(
            date: LocalDate,
            lastPeriodStart: LocalDate,
            cycleLength: Int
        ): CyclePhase {
            val dayInCycle = ChronoUnit.DAYS.between(lastPeriodStart, date).toInt() + 1
            val ovulationDay = (cycleLength - HealthConstants.DEFAULT_LUTEAL_PHASE_LENGTH)
                .coerceIn(HealthConstants.OVULATION_DAY_MIN, cycleLength - 1)

            return when {
                dayInCycle <= HealthConstants.DEFAULT_PERIOD_LENGTH -> MENSTRUAL
                dayInCycle <= ovulationDay - 4 -> FOLLICULAR
                dayInCycle <= ovulationDay + 1 -> OVULATORY
                else -> LUTEAL
            }
        }
    }
}
