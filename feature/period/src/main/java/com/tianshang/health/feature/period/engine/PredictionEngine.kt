package com.tianshang.health.feature.period.engine

import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.entity.DailySymptom
import com.tianshang.health.core.database.entity.PeriodRecord
import com.tianshang.health.core.period.api.Confidence
import com.tianshang.health.core.period.api.CyclePrediction
import com.tianshang.health.core.period.api.PeriodPredictionEngine
import com.tianshang.health.core.period.api.PredictionResult
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

data class CycleStatistics(
    val averageCycleLength: Double,
    val averagePeriodLength: Double,
    val cycleRegularity: String,
    val totalCycles: Int,
    val painTrend: List<Pair<Int, Double>>,
    val symptomFrequency: Map<String, Int>,
    val cycleLengths: List<Pair<Int, Int>>
)

enum class OvulationTestResult(val value: String) {
    POSITIVE("positive"),
    NEGATIVE("negative"),
    UNCLEAR("unclear");

    companion object {
        fun fromValue(value: String?): OvulationTestResult? {
            return entries.find { it.value == value }
        }
    }
}

enum class CervicalMucusType(val value: String) {
    DRY("dry"),
    STICKY("sticky"),
    CREAMY("creamy"),
    WATERY("watery"),
    EGG_WHITE("egg_white");

    companion object {
        fun fromValue(value: String?): CervicalMucusType? {
            return entries.find { it.value == value }
        }
    }
}

@Singleton
class PredictionEngine @Inject constructor() : PeriodPredictionEngine {

    companion object {
        const val DEFAULT_CYCLE_LENGTH = 28
        const val DEFAULT_PERIOD_LENGTH = 5
        const val DEFAULT_LUTEAL_PHASE = 14
        const val MIN_CYCLES_FOR_PREDICTION = 3
        const val DECAY_FACTOR = 0.8
        const val MIN_LUTEAL_PHASE = 10
        const val MAX_LUTEAL_PHASE = 16
        const val MIN_CYCLE_LENGTH = 21
        const val MAX_CYCLE_LENGTH = 45
    }

    override fun predict(
        records: List<PeriodRecord>,
        symptoms: List<DailySymptom>,
        lutealPhaseLength: Int
    ): PredictionResult? {
        if (records.size < MIN_CYCLES_FOR_PREDICTION) {
            return null
        }

        val sortedRecords = records.sortedBy { it.startDate }
        val cycleLengths = calculateCycleLengths(sortedRecords)
        val averageCycleLength = calculateWeightedAverage(cycleLengths)
        val averagePeriodLength = calculateAveragePeriodLength(sortedRecords)

        // Learn luteal phase from data
        val learnedLutealPhase = learnLutealPhase(sortedRecords, symptoms, lutealPhaseLength)

        val lastRecord = sortedRecords.last()
        val lastStartDate = LocalDate.parse(lastRecord.startDate)

        var nextStartDate = lastStartDate.plusDays(averageCycleLength.toLong())

        // Expanded ovulation adjustment range
        val ovulationAdjustment = findOvulationAdjustment(symptoms, lastRecord, averageCycleLength)
        if (ovulationAdjustment != null) {
            nextStartDate = ovulationAdjustment.plusDays(learnedLutealPhase.toLong())
        }

        val confidence = calculateConfidence(cycleLengths)

        // Generate multi-month predictions
        val predictions = mutableListOf<CyclePrediction>()
        for (i in 0 until 6) {
            val periodStart = nextStartDate.plusDays((i * averageCycleLength).toLong())
            val periodEnd = periodStart.plusDays(averagePeriodLength.toLong() - 1)
            val ovulationDate = periodStart.minusDays(learnedLutealPhase.toLong())
            val fertileStart = ovulationDate.minusDays(5)
            val fertileEnd = ovulationDate.plusDays(1)

            predictions.add(
                CyclePrediction(
                    periodStartDate = periodStart,
                    periodEndDate = periodEnd,
                    ovulationDate = ovulationDate,
                    fertileWindowStart = fertileStart,
                    fertileWindowEnd = fertileEnd
                )
            )
        }

        val firstPrediction = predictions.first()
        val explanation = generateExplanation(cycleLengths, averagePeriodLength.toInt(), confidence, sortedRecords.size)

        return PredictionResult(
            nextPeriodStart = firstPrediction.periodStartDate,
            nextPeriodEnd = firstPrediction.periodEndDate,
            ovulationDate = firstPrediction.ovulationDate,
            fertileWindowStart = firstPrediction.fertileWindowStart,
            fertileWindowEnd = firstPrediction.fertileWindowEnd,
            cycleLength = averageCycleLength.toInt(),
            periodLength = averagePeriodLength.toInt(),
            confidence = confidence,
            explanation = explanation,
            predictions = predictions
        )
    }

    // Learn luteal phase from historical data
    private fun learnLutealPhase(
        records: List<PeriodRecord>,
        symptoms: List<DailySymptom>,
        defaultLutealPhase: Int
    ): Int {
        val lutealPhases = mutableListOf<Int>()

        for (i in 1 until records.size) {
            val prevRecord = records[i - 1]
            val currRecord = records[i]

            val searchStart = prevRecord.endDate?.let { LocalDate.parse(it).plusDays(1) }
                ?: LocalDate.parse(prevRecord.startDate).plusDays(DEFAULT_PERIOD_LENGTH.toLong())
            val searchEnd = LocalDate.parse(currRecord.startDate)

            val ovulationDate = findOvulationDateInRange(symptoms, searchStart, searchEnd)

            if (ovulationDate != null) {
                val lutealDays = ChronoUnit.DAYS.between(ovulationDate, searchEnd).toInt()
                if (lutealDays in MIN_LUTEAL_PHASE..MAX_LUTEAL_PHASE) {
                    lutealPhases.add(lutealDays)
                }
            }
        }

        return if (lutealPhases.isNotEmpty()) {
            val sorted = lutealPhases.sorted()
            sorted[sorted.size / 2]
        } else {
            defaultLutealPhase
        }
    }

    // Find ovulation date within a date range
    private fun findOvulationDateInRange(
        symptoms: List<DailySymptom>,
        startDate: LocalDate,
        endDate: LocalDate
    ): LocalDate? {
        val relevantSymptoms = symptoms.filter {
            val symptomDate = LocalDate.parse(it.date)
            !symptomDate.isBefore(startDate) && !symptomDate.isAfter(endDate)
        }

        // Priority 1: Ovulation test positive
        for (symptom in relevantSymptoms) {
            val testResult = OvulationTestResult.fromValue(symptom.ovulationTestResult)
            if (testResult == OvulationTestResult.POSITIVE) {
                return LocalDate.parse(symptom.date)
            }
        }

        // Priority 2: Egg white cervical mucus
        for (symptom in relevantSymptoms) {
            val mucusType = CervicalMucusType.fromValue(symptom.cervicalMucus)
            if (mucusType == CervicalMucusType.EGG_WHITE) {
                return LocalDate.parse(symptom.date)
            }
        }

        // Priority 3: Temperature rise (3 consecutive days > first + 0.3°C)
        val temps = relevantSymptoms.mapNotNull { symptom ->
            symptom.bodyTemperature?.let { LocalDate.parse(symptom.date) to it }
        }
        if (temps.size >= 3) {
            for (i in 0 until temps.size - 2) {
                if (temps[i + 1].second > temps[i].second + HealthConstants.BBT_SHIFT_THRESHOLD &&
                    temps[i + 2].second > temps[i].second + HealthConstants.BBT_SHIFT_THRESHOLD
                ) {
                    return temps[i + 1].first
                }
            }
        }

        return null
    }

    // IQR filtering for cycle lengths
    private fun calculateCycleLengths(records: List<PeriodRecord>): List<Int> {
        val rawLengths = mutableListOf<Int>()
        for (i in 1 until records.size) {
            val prevDate = LocalDate.parse(records[i - 1].startDate)
            val currDate = LocalDate.parse(records[i].startDate)
            val days = ChronoUnit.DAYS.between(prevDate, currDate).toInt()
            rawLengths.add(days)
        }

        if (rawLengths.isEmpty()) return emptyList()

        // Calculate dynamic bounds using IQR
        val sorted = rawLengths.sorted()
        val q1 = sorted[sorted.size / 4]
        val q3 = sorted[sorted.size * 3 / 4]
        val iqr = q3 - q1
        val lowerBound = max(MIN_CYCLE_LENGTH.toDouble(), q1 - 1.5 * iqr)
        val upperBound = min(MAX_CYCLE_LENGTH.toDouble(), q3 + 1.5 * iqr)

        return rawLengths.filter { it.toDouble() in lowerBound..upperBound }
    }

    // Exponential decay weighting
    private fun calculateWeightedAverage(lengths: List<Int>): Double {
        if (lengths.isEmpty()) return DEFAULT_CYCLE_LENGTH.toDouble()

        var sum = 0.0
        var weightSum = 0.0
        lengths.forEachIndexed { index, length ->
            val weight = DECAY_FACTOR.pow((lengths.size - 1 - index).toDouble())
            sum += length * weight
            weightSum += weight
        }

        return sum / weightSum
    }

    // IQR filtering for period lengths
    private fun calculateAveragePeriodLength(records: List<PeriodRecord>): Double {
        val lengths = records.mapNotNull { record ->
            record.endDate?.let {
                val start = LocalDate.parse(record.startDate)
                val end = LocalDate.parse(it)
                ChronoUnit.DAYS.between(start, end).toInt() + 1
            }
        }
        if (lengths.isEmpty()) return DEFAULT_PERIOD_LENGTH.toDouble()

        // IQR filtering
        val sorted = lengths.sorted()
        val q1 = sorted[sorted.size / 4]
        val q3 = sorted[sorted.size * 3 / 4]
        val iqr = q3 - q1
        val lowerBound = q1 - 1.5 * iqr
        val upperBound = q3 + 1.5 * iqr

        val filtered = lengths.filter { it.toDouble() in lowerBound..upperBound }
        return if (filtered.isNotEmpty()) filtered.average() else lengths.average()
    }

    // Expanded ovulation adjustment range
    private fun findOvulationAdjustment(
        symptoms: List<DailySymptom>,
        lastRecord: PeriodRecord,
        averageCycleLength: Double
    ): LocalDate? {
        val lastStartDate = LocalDate.parse(lastRecord.startDate)
        val checkEndDate = lastStartDate.plusDays(averageCycleLength.toLong())

        val relevantSymptoms = symptoms.filter {
            val symptomDate = LocalDate.parse(it.date)
            symptomDate.isAfter(lastStartDate) && !symptomDate.isAfter(checkEndDate)
        }

        relevantSymptoms.forEach { symptom ->
            val testResult = OvulationTestResult.fromValue(symptom.ovulationTestResult)
            val mucusType = CervicalMucusType.fromValue(symptom.cervicalMucus)
            if (testResult == OvulationTestResult.POSITIVE || mucusType == CervicalMucusType.EGG_WHITE) {
                return LocalDate.parse(symptom.date)
            }
        }

        val temps = relevantSymptoms.mapNotNull { symptom ->
            symptom.bodyTemperature?.let { LocalDate.parse(symptom.date) to it }
        }
        if (temps.size >= 3) {
            for (i in 0 until temps.size - 2) {
                if (temps[i + 1].second > temps[i].second + HealthConstants.BBT_SHIFT_THRESHOLD &&
                    temps[i + 2].second > temps[i].second + HealthConstants.BBT_SHIFT_THRESHOLD
                ) {
                    return temps[i + 1].first
                }
            }
        }

        return null
    }

    private fun calculateConfidence(cycleLengths: List<Int>): Confidence {
        if (cycleLengths.size < MIN_CYCLES_FOR_PREDICTION) {
            return Confidence.INSUFFICIENT_DATA
        }

        val mean = cycleLengths.average()
        val variance = cycleLengths.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        val cv = stdDev / mean

        return when {
            cv < 0.05 -> Confidence.HIGH
            cv < 0.1 -> Confidence.MEDIUM
            else -> Confidence.LOW
        }
    }

    private fun generateExplanation(
        cycleLengths: List<Int>,
        periodLength: Int,
        confidence: Confidence,
        cycleCount: Int
    ): String {
        val avgCycle = cycleLengths.average().toInt()

        return "Adaptively predicted based on the last $cycleCount cycles " +
            "(average $avgCycle days/cycle, $periodLength days/period). " +
            "Confidence: ${confidence.name.lowercase().replace('_', ' ')}."
    }

    fun calculateCycleRegularity(cycleLengths: List<Int>): String {
        if (cycleLengths.size < MIN_CYCLES_FOR_PREDICTION) {
            return "Insufficient data"
        }

        val mean = cycleLengths.average()
        val variance = cycleLengths.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        val cv = stdDev / mean

        return when {
            cv < 0.05 -> "Regular"
            cv < 0.1 -> "Somewhat regular"
            else -> "Irregular"
        }
    }

    fun calculateStatistics(
        records: List<PeriodRecord>,
        symptoms: List<DailySymptom>
    ): CycleStatistics {
        val sortedRecords = records.sortedBy { it.startDate }
        val cycleLengths = calculateCycleLengths(sortedRecords)
        val periodLengths = sortedRecords.mapNotNull { record ->
            record.endDate?.let {
                val start = LocalDate.parse(record.startDate)
                val end = LocalDate.parse(it)
                ChronoUnit.DAYS.between(start, end).toInt() + 1
            }
        }

        val avgCycleLength = if (cycleLengths.isNotEmpty()) cycleLengths.average() else 0.0
        val avgPeriodLength = if (periodLengths.isNotEmpty()) periodLengths.average() else 0.0

        val regularity = calculateCycleRegularity(cycleLengths)

        val painTrend = sortedRecords.mapIndexed { index, record ->
            index to (record.painLevel?.toDouble() ?: 0.0)
        }

        val cycleLengthsWithIndex = cycleLengths.mapIndexed { index, length ->
            index to length
        }

        val symptomFrequency = mutableMapOf<String, Int>()
        symptoms.forEach { symptom ->
            val symptomList = parseSymptoms(symptom.symptoms)
            symptomList.forEach { s ->
                symptomFrequency[s] = symptomFrequency.getOrDefault(s, 0) + 1
            }
        }

        return CycleStatistics(
            averageCycleLength = avgCycleLength,
            averagePeriodLength = avgPeriodLength,
            cycleRegularity = regularity,
            totalCycles = sortedRecords.size,
            painTrend = painTrend,
            symptomFrequency = symptomFrequency,
            cycleLengths = cycleLengthsWithIndex
        )
    }

    private fun parseSymptoms(symptomsJson: String?): List<String> {
        if (symptomsJson == null) return emptyList()
        return try {
            symptomsJson.removePrefix("[").removeSuffix("]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotEmpty() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            emptyList()
        }
    }
}
