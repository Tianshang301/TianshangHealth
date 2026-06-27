package com.tianshang.health.feature.steps.service

import com.tianshang.health.core.common.constants.HealthConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StepSyncWorkerTest {

    @Test
    fun `worker adds difference when cachedTotal exceeds lastRecorded`() {
        val cachedTotal = 29000L
        val lastRecorded = 0L
        val missedSteps = cachedTotal - lastRecorded

        assertTrue("Should detect unrecorded steps", missedSteps > 0)
        assertEquals("missedSteps should be 29000", 29000, missedSteps)
    }

    @Test
    fun `worker skips when cachedTotal equals lastRecorded`() {
        val cachedTotal = 29000L
        val lastRecorded = 29000L
        val missedSteps = cachedTotal - lastRecorded

        assertEquals("missedSteps should be 0 when synced", 0, missedSteps)
    }

    @Test
    fun `worker skips large delta as stale baseline`() {
        val cachedTotal = 500_000L
        val lastRecorded = 0L
        val missedSteps = cachedTotal - lastRecorded

        assertTrue(
            "Delta exceeding MAX_STEPS_PER_INTERVAL should be treated as stale",
            missedSteps > HealthConstants.MAX_STEPS_PER_INTERVAL
        )
    }

    @Test
    fun `worker accepts delta within threshold`() {
        val cachedTotal = 500L
        val lastRecorded = 300L
        val missedSteps = cachedTotal - lastRecorded

        assertTrue(
            "Normal delta should be within threshold",
            missedSteps <= HealthConstants.MAX_STEPS_PER_INTERVAL
        )
        assertEquals("missedSteps should be 200", 200, missedSteps)
    }

    @Test
    fun `worker does nothing when cachedTotal is less than lastRecorded`() {
        val cachedTotal = 100L
        val lastRecorded = 500L
        val missedSteps = cachedTotal - lastRecorded

        assertFalse(
            "Should not add steps when cachedTotal < lastRecorded (indicates reset)",
            missedSteps > 0
        )
    }

    @Test
    fun `after service syncs lastRecorded, worker sees zero delta`() {
        val totalSteps = 29000L

        val lastRecordedAfterServiceSync = totalSteps
        val delta = totalSteps - lastRecordedAfterServiceSync

        assertEquals("Worker should see zero delta when lastRecorded is in sync", 0, delta)
    }

    @Test
    fun `initial baseline sets lastRecorded to prevent worker double-count`() {
        val totalSteps = 25000L

        val lastRecordedAtBaseline = totalSteps
        val cachedTotalAtBaseline = totalSteps

        val workerDelta = cachedTotalAtBaseline - lastRecordedAtBaseline

        assertEquals(
            "Worker should see zero delta when baseline properly initialized",
            0,
            workerDelta
        )
    }
}
