package com.tianshang.health.feature.steps.service

import kotlin.math.sqrt

/**
 * Heuristic step detector using accelerometer magnitude peaks.
 *
 * Algorithm:
 * - Compute gravity magnitude from 3-axis acceleration.
 * - Maintain a sliding window to estimate current mean and dynamic threshold.
 * - Detect a step when magnitude crosses above threshold and then falls below,
 *   enforcing minimum step interval (cadence limit) and plausible magnitude range.
 */
class StepDetector {

    private val samples = ArrayDeque<Float>(WINDOW_SIZE)
    private var lastStepTime = 0L
    private var previousMagnitude = 0f
    private var inPeak = false

    fun process(accelerometerValues: FloatArray, timestampMillis: Long): Boolean {
        val magnitude = computeMagnitude(accelerometerValues)
        if (magnitude < MIN_MAGNITUDE || magnitude > MAX_MAGNITUDE) {
            previousMagnitude = magnitude
            return false
        }

        samples.addLast(magnitude)
        if (samples.size > WINDOW_SIZE) {
            samples.removeFirst()
        }

        val baseline = samples.average().toFloat()
        val dynamicThreshold = (baseline * THRESHOLD_RATIO).coerceAtLeast(MIN_THRESHOLD)

        val isStep = detectPeak(magnitude, baseline, dynamicThreshold, timestampMillis)

        previousMagnitude = magnitude
        return isStep
    }

    private fun detectPeak(
        magnitude: Float,
        baseline: Float,
        threshold: Float,
        timestampMillis: Long
    ): Boolean {
        val deviation = magnitude - baseline
        val previousDeviation = previousMagnitude - baseline

        // Rising edge crossing threshold
        if (deviation > threshold && previousDeviation <= threshold && !inPeak) {
            inPeak = true
        }

        // Falling edge: magnitude returns below baseline-ish and enough time passed
        if (inPeak && deviation < 0f && previousDeviation >= 0f) {
            inPeak = false
            if (timestampMillis - lastStepTime >= MIN_STEP_INTERVAL_MS) {
                lastStepTime = timestampMillis
                return true
            }
        }

        // Reset missed peaks only when deviation is clearly below baseline,
        // so the normal falling-edge crossing is not short-circuited.
        if (deviation < -threshold) {
            inPeak = false
        }

        return false
    }

    private fun computeMagnitude(values: FloatArray): Float {
        val x = values[0]
        val y = values[1]
        val z = values[2]
        return sqrt(x * x + y * y + z * z)
    }

    fun reset() {
        samples.clear()
        lastStepTime = 0L
        previousMagnitude = 0f
        inPeak = false
    }

    companion object {
        private const val WINDOW_SIZE = 30
        private const val THRESHOLD_RATIO = 0.25f
        private const val MIN_THRESHOLD = 1.0f
        private const val MIN_STEP_INTERVAL_MS = 250L
        private const val MIN_MAGNITUDE = 8.0f
        private const val MAX_MAGNITUDE = 16.0f
    }
}
