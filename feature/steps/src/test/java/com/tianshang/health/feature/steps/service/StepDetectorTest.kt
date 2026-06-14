package com.tianshang.health.feature.steps.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.cos

class StepDetectorTest {

    private lateinit var detector: StepDetector

    @Before
    fun setup() {
        detector = StepDetector()
    }

    @Test
    fun `empty or low magnitude samples do not produce steps`() {
        val gravity = floatArrayOf(0f, 0f, 9.8f)
        assertFalse(detector.process(gravity, 0L))
        assertFalse(detector.process(gravity, 100L))
        assertFalse(detector.process(gravity, 200L))
    }

    @Test
    fun `magnitude outside plausible range is ignored`() {
        val tooLow = floatArrayOf(0f, 0f, 2f)
        val tooHigh = floatArrayOf(0f, 0f, 20f)

        assertFalse(detector.process(tooLow, 0L))
        assertFalse(detector.process(tooHigh, 100L))
    }

    @Test
    fun `regular walking pattern produces expected step count`() {
        val stepCount = simulateSteps(stepCount = 10, stepIntervalMs = 500L)
        assertTrue("Should detect at least 8 of 10 steps", stepCount >= 8)
    }

    @Test
    fun `steps faster than minimum cadence are filtered`() {
        val stepCount = simulateSteps(stepCount = 20, stepIntervalMs = 100L)
        assertTrue("Rapid peaks should be filtered by cadence limit", stepCount < 10)
    }

    @Test
    fun `reset clears internal state`() {
        simulateSteps(stepCount = 5, stepIntervalMs = 500L)
        detector.reset()
        val afterReset = simulateSteps(stepCount = 3, stepIntervalMs = 500L)
        assertEquals("After reset only new steps should be counted", 3, afterReset)
    }

    @Test
    fun `small vibration noise does not produce false steps`() {
        var detected = 0
        var timestamp = 0L
        repeat(50) {
            val noise = floatArrayOf(0f, 0f, 9.8f + 0.3f * cos(it * 0.5).toFloat())
            if (detector.process(noise, timestamp)) detected++
            timestamp += 50L
        }
        assertEquals("Small noise should not trigger steps", 0, detected)
    }

    private fun simulateSteps(stepCount: Int, stepIntervalMs: Long): Int {
        var detected = 0
        var timestamp = 0L
        val samplesPerStep = 20
        val baselineSamples = 8
        val riseSamples = 4
        val peakSamples = 4
        val fallSamples = 4
        val sampleInterval = stepIntervalMs / samplesPerStep

        repeat(stepCount) {
            repeat(baselineSamples) {
                if (detector.process(floatArrayOf(0f, 0f, 10f), timestamp)) detected++
                timestamp += sampleInterval
            }
            repeat(riseSamples) { index ->
                val t = (index + 1) / riseSamples.toFloat()
                val z = 10f + 5f * t
                if (detector.process(floatArrayOf(0f, 0f, z), timestamp)) detected++
                timestamp += sampleInterval
            }
            repeat(peakSamples) {
                if (detector.process(floatArrayOf(0f, 0f, 15f), timestamp)) detected++
                timestamp += sampleInterval
            }
            repeat(fallSamples) { index ->
                val t = 1f - (index + 1) / fallSamples.toFloat()
                val z = 10f + 5f * t
                if (detector.process(floatArrayOf(0f, 0f, z), timestamp)) detected++
                timestamp += sampleInterval
            }
        }
        return detected
    }
}
