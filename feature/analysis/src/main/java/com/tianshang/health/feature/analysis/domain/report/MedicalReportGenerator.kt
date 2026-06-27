package com.tianshang.health.feature.analysis.domain.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.tianshang.health.core.database.entity.DailyHealth
import com.tianshang.health.core.database.entity.PeriodRecord
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Generates medical-grade health reports as PDF with proper pagination.
 * All data is processed locally - no network required.
 */
class MedicalReportGenerator(private val context: Context) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Page dimensions (A4 in points: 595 x 842)
    private val pageWidth = 595f
    private val pageHeight = 842f
    private val marginLeft = 50f
    private val marginRight = 50f
    private val marginTop = 50f
    private val marginBottom = 60f
    private val contentWidth = pageWidth - marginLeft - marginRight

    // Paints
    private val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 28f
        isFakeBoldText = true
    }
    private val headingPaint = Paint().apply {
        color = Color.BLACK
        textSize = 18f
        isFakeBoldText = true
    }
    private val subHeadingPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 14f
        isFakeBoldText = true
    }
    private val bodyPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 13f
    }
    private val smallPaint = Paint().apply {
        color = Color.GRAY
        textSize = 11f
    }
    private val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
    }
    private val tableHeaderPaint = Paint().apply {
        color = Color.WHITE
        textSize = 11f
        isFakeBoldText = true
    }
    private val tableCellPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 11f
    }
    private val tableHeaderBgPaint = Paint().apply {
        color = Color.parseColor("#5B6EF5")
    }
    private val tableRowBgPaint = Paint().apply {
        color = Color.parseColor("#F5F5F5")
    }

    data class HealthReportData(
        val userName: String,
        val gender: String,
        val reportPeriod: String,
        val periodRecords: List<PeriodRecord>,
        val dailyHealthData: List<DailyHealth>,
        val generatedAt: String = LocalDate.now().toString(),
        val includePeriod: Boolean = true,
        val includeActivity: Boolean = true,
        val includeSleep: Boolean = true,
        val includeNutrition: Boolean = true
    )

    private data class PageContext(
        var canvas: Canvas,
        var pageNumber: Int,
        var currentY: Float
    )

    /**
     * Generates a PDF report with automatic pagination and returns the file URI.
     */
    fun generateReport(data: HealthReportData): android.net.Uri? {
        val document = PdfDocument()
        var pageNumber = 1

        try {
            // Create first page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var currentY = marginTop

            // Draw header (always on first page)
            currentY = drawReportHeader(canvas, data, currentY)
            currentY += 20f

            // Helper to create new page when needed
            fun ensureSpace(neededHeight: Float): Boolean {
                if (currentY + neededHeight > pageHeight - marginBottom) {
                    // Finish current page with footer
                    drawFooter(canvas, pageNumber)
                    document.finishPage(page)

                    // Create new page
                    pageNumber++
                    val newPageInfo = PdfDocument.PageInfo.Builder(
                        pageWidth.toInt(),
                        pageHeight.toInt(),
                        pageNumber
                    ).create()
                    page = document.startPage(newPageInfo)
                    canvas = page.canvas
                    currentY = marginTop
                    return true
                }
                return false
            }

            // Draw sections (only selected ones)
            if (data.includePeriod) {
                currentY = drawPeriodSummary(canvas, data, currentY, ::ensureSpace)
            }
            if (data.includeActivity) {
                currentY = drawActivitySummary(canvas, data, currentY, ::ensureSpace)
            }
            if (data.includeSleep) {
                currentY = drawSleepSummary(canvas, data, currentY, ::ensureSpace)
            }
            if (data.includeNutrition) {
                currentY = drawNutritionSummary(canvas, data, currentY, ::ensureSpace)
            }

            // Final footer
            drawFooter(canvas, pageNumber)
            document.finishPage(page)

            // Save to app-specific directory
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "health_report_${data.generatedAt}.pdf"
            )
            FileOutputStream(file).use { output ->
                document.writeTo(output)
            }

            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
            Log.e("MedicalReportGenerator", "Failed to generate report", e)
            return null
        } finally {
            document.close()
        }
    }

    private fun drawReportHeader(canvas: Canvas, data: HealthReportData, startY: Float): Float {
        var y = startY

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_health_report),
            marginLeft,
            y,
            titlePaint
        )
        y += 35f

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_generated, data.generatedAt),
            marginLeft,
            y,
            smallPaint
        )
        y += 18f
        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_user_info, data.userName, data.gender),
            marginLeft,
            y,
            bodyPaint
        )
        y += 18f
        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_report_period, data.reportPeriod),
            marginLeft,
            y,
            bodyPaint
        )
        y += 15f

        canvas.drawLine(marginLeft, y, pageWidth - marginRight, y, linePaint)

        return y
    }

    private fun drawPeriodSummary(
        canvas: Canvas,
        data: HealthReportData,
        startY: Float,
        ensureSpace: (Float) -> Boolean
    ): Float {
        var y = startY + 20f

        val estimatedHeight = 120f + (data.periodRecords.size.coerceAtMost(5) * 20f)
        if (ensureSpace(estimatedHeight)) {
            y = marginTop
        }

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_period_summary),
            marginLeft,
            y,
            headingPaint
        )
        y += 25f

        if (data.periodRecords.isEmpty()) {
            canvas.drawText(
                context.getString(com.tianshang.health.core.common.R.string.pdf_no_period_records),
                marginLeft,
                y,
                bodyPaint
            )
            return y + 20f
        }

        val totalCycles = data.periodRecords.size
        val avgCycleLength = if (totalCycles > 1) {
            val lengths = mutableListOf<Int>()
            for (i in 1 until data.periodRecords.size) {
                val prev = safeParseDate(data.periodRecords[i - 1].startDate)
                val curr = safeParseDate(data.periodRecords[i].startDate)
                if (prev != null && curr != null) {
                    lengths.add(java.time.temporal.ChronoUnit.DAYS.between(prev, curr).toInt())
                }
            }
            if (lengths.isNotEmpty()) lengths.average().toInt() else 0
        } else {
            0
        }

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_total_cycles, totalCycles),
            marginLeft,
            y,
            bodyPaint
        )
        y += 20f
        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_avg_cycle_length, avgCycleLength),
            marginLeft,
            y,
            bodyPaint
        )
        y += 20f

        val avgPain = data.periodRecords.mapNotNull { it.painLevel }.average()
        if (!avgPain.isNaN()) {
            canvas.drawText(
                context.getString(com.tianshang.health.core.common.R.string.pdf_avg_pain, avgPain),
                marginLeft,
                y,
                bodyPaint
            )
            y += 20f
        }

        y += 10f
        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_recent_records),
            marginLeft,
            y,
            subHeadingPaint
        )
        y += 20f

        // Table
        val colWidths = floatArrayOf(120f, 120f, 80f, 80f)
        val colX = floatArrayOf(marginLeft, marginLeft + 120f, marginLeft + 240f, marginLeft + 320f)

        drawTableRow(
            canvas,
            colX,
            y,
            colWidths,
            arrayOf(
                context.getString(com.tianshang.health.core.common.R.string.pdf_start_date),
                context.getString(com.tianshang.health.core.common.R.string.pdf_end_date),
                context.getString(com.tianshang.health.core.common.R.string.pdf_flow),
                context.getString(com.tianshang.health.core.common.R.string.pdf_pain)
            ),
            isHeader = true
        )
        y += 22f

        data.periodRecords.takeLast(5).forEachIndexed { index, record ->
            val endStr = record.endDate ?: context.getString(com.tianshang.health.core.common.R.string.pdf_ongoing)
            val flowStr = when (record.flowLevel) {
                1 -> context.getString(com.tianshang.health.core.common.R.string.pdf_flow_light)
                2 -> context.getString(com.tianshang.health.core.common.R.string.pdf_flow_medium)
                3 -> context.getString(com.tianshang.health.core.common.R.string.pdf_flow_heavy)
                else -> "-"
            }
            val painStr = when (record.painLevel) {
                0 -> context.getString(com.tianshang.health.core.common.R.string.pdf_pain_none)
                1 -> context.getString(com.tianshang.health.core.common.R.string.pdf_pain_mild)
                2 -> context.getString(com.tianshang.health.core.common.R.string.pdf_pain_moderate)
                3 -> context.getString(com.tianshang.health.core.common.R.string.pdf_pain_severe)
                else -> "-"
            }

            drawTableRow(
                canvas,
                colX,
                y,
                colWidths,
                arrayOf(record.startDate, endStr, flowStr, painStr),
                isHeader = false,
                alternate = index % 2 == 1
            )
            y += 20f
        }

        return y + 10f
    }

    private fun drawActivitySummary(
        canvas: Canvas,
        data: HealthReportData,
        startY: Float,
        ensureSpace: (Float) -> Boolean
    ): Float {
        var y = startY + 20f

        if (ensureSpace(100f)) {
            y = marginTop
        }

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_activity_summary),
            marginLeft,
            y,
            headingPaint
        )
        y += 25f

        val stepsData = data.dailyHealthData.mapNotNull { it.steps }
        if (stepsData.isEmpty()) {
            canvas.drawText(
                context.getString(com.tianshang.health.core.common.R.string.pdf_no_step_data),
                marginLeft,
                y,
                bodyPaint
            )
            return y + 20f
        }

        val avgSteps = stepsData.average().toInt()
        val totalSteps = stepsData.sum()
        val maxSteps = stepsData.maxOrNull() ?: 0

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_avg_daily_steps, avgSteps),
            marginLeft,
            y,
            bodyPaint
        )
        y += 20f
        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_total_steps, totalSteps),
            marginLeft,
            y,
            bodyPaint
        )
        y += 20f
        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_max_steps, maxSteps),
            marginLeft,
            y,
            bodyPaint
        )
        y += 20f

        val exerciseData = data.dailyHealthData.filter {
            val minutes = it.exerciseMinutes
            minutes != null && minutes > 0
        }
        if (exerciseData.isNotEmpty()) {
            val totalExercise = exerciseData.sumOf { it.exerciseMinutes ?: 0 }
            canvas.drawText(
                context.getString(com.tianshang.health.core.common.R.string.pdf_total_exercise, totalExercise),
                marginLeft,
                y,
                bodyPaint
            )
            y += 20f
        }

        return y
    }

    private fun drawSleepSummary(
        canvas: Canvas,
        data: HealthReportData,
        startY: Float,
        ensureSpace: (Float) -> Boolean
    ): Float {
        var y = startY + 20f

        if (ensureSpace(80f)) {
            y = marginTop
        }

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_sleep_summary),
            marginLeft,
            y,
            headingPaint
        )
        y += 25f

        val sleepData = data.dailyHealthData.mapNotNull { it.sleepHours }
        if (sleepData.isEmpty()) {
            canvas.drawText(
                context.getString(com.tianshang.health.core.common.R.string.pdf_no_sleep_data),
                marginLeft,
                y,
                bodyPaint
            )
            return y + 20f
        }

        val avgSleep = sleepData.average()
        val avgQuality = data.dailyHealthData.mapNotNull { it.sleepQuality }.average()

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_avg_sleep, avgSleep),
            marginLeft,
            y,
            bodyPaint
        )
        y += 20f
        if (!avgQuality.isNaN()) {
            canvas.drawText(
                context.getString(com.tianshang.health.core.common.R.string.pdf_avg_sleep_quality, avgQuality),
                marginLeft,
                y,
                bodyPaint
            )
            y += 20f
        }

        val deepSleepData = data.dailyHealthData.mapNotNull { it.deepSleepHours }
        if (deepSleepData.isNotEmpty()) {
            canvas.drawText(
                context.getString(
                    com.tianshang.health.core.common.R.string.pdf_avg_deep_sleep,
                    deepSleepData.average()
                ),
                marginLeft,
                y,
                bodyPaint
            )
            y += 20f
        }

        return y
    }

    private fun drawNutritionSummary(
        canvas: Canvas,
        data: HealthReportData,
        startY: Float,
        ensureSpace: (Float) -> Boolean
    ): Float {
        var y = startY + 20f

        if (ensureSpace(80f)) {
            y = marginTop
        }

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_nutrition_summary),
            marginLeft,
            y,
            headingPaint
        )
        y += 25f

        val caloriesData = data.dailyHealthData.mapNotNull { it.caloriesIntake }
        if (caloriesData.isEmpty()) {
            canvas.drawText(
                context.getString(com.tianshang.health.core.common.R.string.pdf_no_nutrition_data),
                marginLeft,
                y,
                bodyPaint
            )
            return y + 20f
        }

        val avgCalories = caloriesData.average()
        val avgWater = data.dailyHealthData.mapNotNull { it.waterIntake }.average()
        val avgProtein = data.dailyHealthData.mapNotNull { it.proteinGrams }.average()

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_avg_calories, avgCalories),
            marginLeft,
            y,
            bodyPaint
        )
        y += 20f
        if (!avgWater.isNaN()) {
            canvas.drawText(
                context.getString(com.tianshang.health.core.common.R.string.pdf_avg_water, avgWater),
                marginLeft,
                y,
                bodyPaint
            )
            y += 20f
        }
        if (!avgProtein.isNaN()) {
            canvas.drawText(
                context.getString(com.tianshang.health.core.common.R.string.pdf_avg_protein, avgProtein),
                marginLeft,
                y,
                bodyPaint
            )
            y += 20f
        }

        return y
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int) {
        val y = pageHeight - 30f

        canvas.drawLine(marginLeft, y - 15f, pageWidth - marginRight, y - 15f, linePaint)

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_footer),
            marginLeft,
            y,
            smallPaint
        )

        val pageText = context.getString(com.tianshang.health.core.common.R.string.pdf_page, pageNumber)
        val textWidth = smallPaint.measureText(pageText)
        canvas.drawText(pageText, pageWidth - marginRight - textWidth, y, smallPaint)

        canvas.drawText(
            context.getString(com.tianshang.health.core.common.R.string.pdf_app_name),
            marginLeft,
            y + 14f,
            smallPaint
        )
    }

    private fun drawTableRow(
        canvas: Canvas,
        colX: FloatArray,
        y: Float,
        colWidths: FloatArray,
        values: Array<String>,
        isHeader: Boolean,
        alternate: Boolean = false
    ) {
        if (isHeader) {
            val rect = RectF(colX[0], y - 14f, colX.last() + colWidths.last(), y + 6f)
            canvas.drawRect(rect, tableHeaderBgPaint)
        } else if (alternate) {
            val rect = RectF(colX[0], y - 14f, colX.last() + colWidths.last(), y + 6f)
            canvas.drawRect(rect, tableRowBgPaint)
        }

        val paint = if (isHeader) tableHeaderPaint else tableCellPaint
        values.forEachIndexed { index, value ->
            canvas.drawText(value, colX[index] + 5f, y, paint)
        }
    }

    private fun safeParseDate(dateStr: String?): LocalDate? {
        return try {
            dateStr?.let { LocalDate.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
