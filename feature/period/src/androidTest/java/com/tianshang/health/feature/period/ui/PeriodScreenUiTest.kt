package com.tianshang.health.feature.period.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.tianshang.health.feature.period.engine.PredictionEngine
import com.tianshang.health.feature.period.ui.components.PredictionCard
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for PeriodScreen critical components.
 * Verifies PredictionCard renders correctly without Hilt dependencies.
 */
class PeriodScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun predictionCard_rendersWithThreePeriodRecords() {
        // This test verifies the fix for MissingFormatArgumentException
        // that occurred when confidence_label required 2 args but only 1 was passed
        val records = listOf(
            com.tianshang.health.core.database.entity.PeriodRecord(id = 1, userId = 1, startDate = "2026-01-01", endDate = "2026-01-05"),
            com.tianshang.health.core.database.entity.PeriodRecord(id = 2, userId = 1, startDate = "2026-01-29", endDate = "2026-02-02"),
            com.tianshang.health.core.database.entity.PeriodRecord(id = 3, userId = 1, startDate = "2026-02-26", endDate = "2026-03-02")
        )

        val engine = PredictionEngine()
        val prediction = engine.predict(records)

        assert(prediction != null)

        composeTestRule.setContent {
            PredictionCard(
                nextPeriodDate = prediction!!.nextPeriodStart.toString(),
                ovulationDate = prediction.ovulationDate.toString(),
                fertileWindow = "${prediction.fertileWindowStart} - ${prediction.fertileWindowEnd}",
                cycleLength = prediction.cycleLength,
                confidence = when (prediction.confidence) {
                    com.tianshang.health.feature.period.engine.Confidence.HIGH -> "High"
                    com.tianshang.health.feature.period.engine.Confidence.MEDIUM -> "Medium"
                    com.tianshang.health.feature.period.engine.Confidence.LOW -> "Low"
                    com.tianshang.health.feature.period.engine.Confidence.INSUFFICIENT_DATA -> "Insufficient Data"
                },
                cycleCount = records.size
            )
        }

        composeTestRule.waitForIdle()

        // If we reach here without crash, the format string bug is fixed
        // The card renders successfully with confidence + cycleLength parameters
        assert(true)
    }

    @Test
    fun predictionCard_withHighConfidence_showsCorrectInfo() {
        // Create highly regular cycles
        val records = (0 until 6).map { i ->
            val month = 1 + i
            com.tianshang.health.core.database.entity.PeriodRecord(
                id = i.toLong(),
                userId = 1,
                startDate = "2026-${month.toString().padStart(2, '0')}-01",
                endDate = "2026-${month.toString().padStart(2, '0')}-06"
            )
        }

        val engine = PredictionEngine()
        val prediction = engine.predict(records)

        assert(prediction != null)
        assert(prediction!!.confidence == com.tianshang.health.feature.period.engine.Confidence.HIGH)

        composeTestRule.setContent {
            PredictionCard(
                nextPeriodDate = prediction.nextPeriodStart.toString(),
                ovulationDate = prediction.ovulationDate.toString(),
                fertileWindow = "${prediction.fertileWindowStart} - ${prediction.fertileWindowEnd}",
                cycleLength = prediction.cycleLength,
                confidence = when (prediction.confidence) {
                    com.tianshang.health.feature.period.engine.Confidence.HIGH -> "High"
                    com.tianshang.health.feature.period.engine.Confidence.MEDIUM -> "Medium"
                    com.tianshang.health.feature.period.engine.Confidence.LOW -> "Low"
                    com.tianshang.health.feature.period.engine.Confidence.INSUFFICIENT_DATA -> "Insufficient Data"
                },
                cycleCount = records.size
            )
        }

        composeTestRule.waitForIdle()

        // Verify confidence label shows "High"
        composeTestRule.onNodeWithText("High", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }
}
