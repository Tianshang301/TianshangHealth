package com.tianshang.health.core.period.api

import java.time.LocalDate

data class PredictionResult(
    val nextPeriodStart: LocalDate,
    val nextPeriodEnd: LocalDate,
    val ovulationDate: LocalDate,
    val fertileWindowStart: LocalDate,
    val fertileWindowEnd: LocalDate,
    val cycleLength: Int,
    val periodLength: Int,
    val confidence: Confidence,
    val explanation: String,
    val predictions: List<CyclePrediction> = emptyList()
)

data class CyclePrediction(
    val periodStartDate: LocalDate,
    val periodEndDate: LocalDate,
    val ovulationDate: LocalDate,
    val fertileWindowStart: LocalDate,
    val fertileWindowEnd: LocalDate
)
