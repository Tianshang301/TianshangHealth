package com.tianshang.health.core.period.api

import com.tianshang.health.core.database.entity.DailySymptom
import com.tianshang.health.core.database.entity.PeriodRecord

interface PeriodPredictionEngine {
    fun predict(
        records: List<PeriodRecord>,
        symptoms: List<DailySymptom> = emptyList(),
        lutealPhaseLength: Int = DEFAULT_LUTEAL_PHASE
    ): PredictionResult?
}
