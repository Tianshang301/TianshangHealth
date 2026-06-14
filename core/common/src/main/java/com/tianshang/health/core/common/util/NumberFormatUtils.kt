package com.tianshang.health.core.common.util

import java.util.Locale

object NumberFormatUtils {

    private const val THOUSAND = 1000
    private const val TEN_THOUSAND = 10_000
    private const val MILLION = 1_000_000

    fun formatCompactNumber(value: Int, locale: Locale = Locale.getDefault()): String =
        formatCompactNumber(value.toLong(), locale)

    fun formatCompactNumber(value: Float, locale: Locale = Locale.getDefault()): String =
        formatCompactNumber(value.toLong(), locale)

    private fun formatCompactNumber(value: Long, locale: Locale): String {
        if (value < THOUSAND) {
            return value.toString()
        }

        return if (isCjkLocale(locale)) {
            formatCjkCompactNumber(value, locale)
        } else {
            formatWesternCompactNumber(value)
        }
    }

    fun formatExactNumber(value: Int, locale: Locale = Locale.getDefault()): String =
        String.format(locale, "%,d", value)

    private fun isCjkLocale(locale: Locale): Boolean {
        val language = locale.language
        return language == Locale.CHINESE.language ||
            language == Locale.JAPANESE.language ||
            language == Locale.KOREAN.language
    }

    @Suppress("MagicNumber")
    private fun formatCjkCompactNumber(value: Long, locale: Locale): String {
        val tenThousands = value / TEN_THOUSAND.toDouble()
        val formatted = formatWithOptionalDecimal(tenThousands)
        return if (locale.language == Locale.KOREAN.language) {
            "${formatted}만"
        } else {
            "${formatted}万"
        }
    }

    @Suppress("MagicNumber")
    private fun formatWesternCompactNumber(value: Long): String {
        return if (value >= MILLION) {
            val millions = value / MILLION.toDouble()
            "${formatWithOptionalDecimal(millions)}M"
        } else {
            val thousands = value / THOUSAND.toDouble()
            "${formatWithOptionalDecimal(thousands)}k"
        }
    }

    private fun formatWithOptionalDecimal(value: Double): String {
        val formatted = String.format(Locale.US, "%.1f", value)
        return if (formatted.endsWith(".0")) {
            formatted.dropLast(2)
        } else {
            formatted
        }
    }
}
