package com.tianshang.health.feature.analysis.ml

import com.tianshang.health.core.common.constants.HealthConstants
import com.tianshang.health.core.database.entity.DailySymptom
import com.tianshang.health.core.database.entity.PeriodRecord
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class FeatureExtractor @Inject constructor() {

    data class PeriodFeatures(
        val cycleLengths: List<Int>,
        val periodLengths: List<Int>,
        val avgPainLevels: List<Float>,
        val lutealPhaseLength: Int,
        val bbtSlope: Float,
        val symptomDiversityIndex: Int
    )

    data class NormalizedInput(
        val array: FloatArray,
        val cycleCount: Int,
        val isColdStart: Boolean
    )

    private val jsonRegex = Regex("[\\[\\]\"]")

    fun extractPeriodFeatures(
        records: List<PeriodRecord>,
        symptoms: List<DailySymptom>,
        defaultLutealPhase: Int = HealthConstants.DEFAULT_LUTEAL_PHASE_LENGTH
    ): PeriodFeatures? {
        val sorted = records.sortedBy { it.startDate }
        if (sorted.size < 2) return null

        val cycleLengths = mutableListOf<Int>()
        val periodLengths = mutableListOf<Int>()
        val avgPainLevels = mutableListOf<Float>()

        for (i in 1 until sorted.size) {
            val prev = LocalDate.parse(sorted[i - 1].startDate)
            val curr = LocalDate.parse(sorted[i].startDate)
            val cycleLen = ChronoUnit.DAYS.between(prev, curr).toInt()
            cycleLengths.add(cycleLen)

            val periodLen = sorted[i].endDate?.let {
                ChronoUnit.DAYS.between(curr, LocalDate.parse(it)).toInt() + 1
            } ?: HealthConstants.DEFAULT_PERIOD_LENGTH
            periodLengths.add(periodLen)

            val pain = sorted[i].painLevel?.toFloat() ?: 0f
            avgPainLevels.add(pain)
        }

        val lutealPhaseLength = estimateLutealPhase(sorted, symptoms, defaultLutealPhase)
        val bbtSlope = calculateBbtSlope(symptoms)
        val symptomDiversityIndex = calculateSymptomDiversity(symptoms)

        return PeriodFeatures(
            cycleLengths = cycleLengths,
            periodLengths = periodLengths,
            avgPainLevels = avgPainLevels,
            lutealPhaseLength = lutealPhaseLength,
            bbtSlope = bbtSlope,
            symptomDiversityIndex = symptomDiversityIndex
        )
    }

    fun normalizeForLinear(features: PeriodFeatures, spec: ModelSpec): NormalizedInput {
        val recentCycle = features.cycleLengths.lastOrNull()?.toFloat() ?: HealthConstants.DEFAULT_CYCLE_LENGTH.toFloat()
        val avgCycle = features.cycleLengths.average().toFloat()
        val recentPeriod = features.periodLengths.lastOrNull()?.toFloat() ?: HealthConstants.DEFAULT_PERIOD_LENGTH.toFloat()

        val array = FloatArray(6)
        array[0] = normalize(recentCycle, spec.mean[0], spec.std[0])
        array[1] = normalize(features.lutealPhaseLength.toFloat(), spec.mean[1], spec.std[1])
        array[2] = normalize(recentPeriod, spec.mean[2], spec.std[2])
        array[3] = normalize(avgCycle, spec.mean[3], spec.std[3])
        array[4] = 0f
        array[5] = 0f

        return NormalizedInput(
            array = array,
            cycleCount = features.cycleLengths.size,
            isColdStart = features.cycleLengths.size < ModelRegistry.MIN_CYCLES_FOR_LINEAR
        )
    }

    fun normalizeForLstm(features: PeriodFeatures, spec: ModelSpec): NormalizedInput {
        val array = FloatArray(LSTM_WINDOW * 6)
        val fillCount = minOf(features.cycleLengths.size, LSTM_WINDOW)
        val offset = LSTM_WINDOW - fillCount

        for (i in 0 until fillCount) {
            val idx = features.cycleLengths.size - fillCount + i
            val base = (offset + i) * 6
            val cycleLen = features.cycleLengths.getOrElse(idx) { HealthConstants.DEFAULT_CYCLE_LENGTH }.toFloat()
            val periodLen = features.periodLengths.getOrElse(idx) { HealthConstants.DEFAULT_PERIOD_LENGTH }.toFloat()
            val lutealPhase = features.lutealPhaseLength.toFloat()
            val avgCycle = features.cycleLengths.take(idx + 1).average().toFloat().coerceAtLeast(cycleLen)

            array[base] = normalize(cycleLen, spec.mean[0], spec.std[0])
            array[base + 1] = normalize(lutealPhase, spec.mean[1], spec.std[1])
            array[base + 2] = normalize(periodLen, spec.mean[2], spec.std[2])
            array[base + 3] = normalize(avgCycle, spec.mean[3], spec.std[3])
            array[base + 4] = 0f
            array[base + 5] = 0f
        }

        return NormalizedInput(
            array = array,
            cycleCount = features.cycleLengths.size,
            isColdStart = features.cycleLengths.size < ModelRegistry.MIN_CYCLES_FOR_LSTM
        )
    }

    fun denormalizePrediction(
        normalizedValue: Float,
        outputMean: Float,
        outputStd: Float
    ): Float {
        return normalizedValue * outputStd + outputMean
    }

    private fun normalize(value: Float, mean: Float, std: Float): Float {
        return if (std != 0f) (value - mean) / std else 0f
    }

    private fun estimateLutealPhase(
        records: List<PeriodRecord>,
        symptoms: List<DailySymptom>,
        defaultLutealPhase: Int
    ): Int {
        val lutealPhases = mutableListOf<Int>()

        for (i in 1 until records.size) {
            val prevRecord = records[i - 1]
            val currRecord = records[i]

            val searchStart = prevRecord.endDate?.let {
                LocalDate.parse(it).plusDays(1)
            } ?: LocalDate.parse(prevRecord.startDate).plusDays(5)
            val searchEnd = LocalDate.parse(currRecord.startDate)

            val ovulationDate = findOvulationDate(symptoms, searchStart, searchEnd)
            if (ovulationDate != null) {
                val lutealDays = ChronoUnit.DAYS.between(ovulationDate, searchEnd).toInt()
                if (lutealDays in MIN_LUTEAL_PHASE..MAX_LUTEAL_PHASE) {
                    lutealPhases.add(lutealDays)
                }
            }
        }

        if (lutealPhases.isEmpty()) return defaultLutealPhase

        val decayFactor = 0.75
        var sum = 0.0
        var weightSum = 0.0
        lutealPhases.forEachIndexed { index, phase ->
            val weight = decayFactor.pow((lutealPhases.size - 1 - index).toDouble())
            sum += phase * weight
            weightSum += weight
        }

        return (sum / weightSum).toInt().coerceIn(MIN_LUTEAL_PHASE, MAX_LUTEAL_PHASE)
    }

    private fun findOvulationDate(
        symptoms: List<DailySymptom>,
        startDate: LocalDate,
        endDate: LocalDate
    ): LocalDate? {
        val relevant = symptoms.filter {
            val d = LocalDate.parse(it.date)
            !d.isBefore(startDate) && !d.isAfter(endDate)
        }

        relevant.forEach { symptom ->
            if (symptom.ovulationTestResult == "positive") {
                return LocalDate.parse(symptom.date)
            }
        }

        relevant.forEach { symptom ->
            if (symptom.cervicalMucus == "egg_white") {
                return LocalDate.parse(symptom.date)
            }
        }

        val temps = relevant.mapNotNull { symptom ->
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

    private fun calculateBbtSlope(symptoms: List<DailySymptom>): Float {
        val temps = symptoms.mapNotNull { symptom ->
            symptom.bodyTemperature?.let { LocalDate.parse(symptom.date) to it }
        }.takeLast(HealthConstants.BBT_SLOPE_WINDOW_DAYS)

        if (temps.size < HealthConstants.BBT_MIN_DAYS_FOR_SLOPE) return 0f

        val n = temps.size
        val x = (0 until n).map { it.toDouble() }
        val y = temps.map { it.second.toDouble() }

        val xMean = x.average()
        val yMean = y.average()

        val numerator = x.zip(y).sumOf { (xi, yi) -> (xi - xMean) * (yi - yMean) }
        val denominator = x.sumOf { (it - xMean) * (it - xMean) }

        return if (denominator != 0.0) (numerator / denominator).toFloat() else 0f
    }

    private fun calculateSymptomDiversity(symptoms: List<DailySymptom>): Int {
        val allSymptoms = mutableSetOf<String>()
        symptoms.forEach { symptom ->
            parseSymptoms(symptom.symptoms).forEach { s ->
                allSymptoms.add(s)
            }
        }
        return allSymptoms.size
    }

    private fun parseSymptoms(symptomsJson: String?): List<String> {
        if (symptomsJson == null) return emptyList()
        return try {
            symptomsJson.replace(jsonRegex, "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val LSTM_WINDOW = 12
        private const val MIN_LUTEAL_PHASE = 10
        private const val MAX_LUTEAL_PHASE = 16
    }
}
