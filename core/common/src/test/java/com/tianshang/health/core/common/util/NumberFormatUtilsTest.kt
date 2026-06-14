package com.tianshang.health.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class NumberFormatUtilsTest {

    @Test
    fun `formatCompactNumber en-US below thousand returns raw`() {
        assertEquals("999", NumberFormatUtils.formatCompactNumber(999, Locale.US))
    }

    @Test
    fun `formatCompactNumber en-US at thousand returns k`() {
        assertEquals("1k", NumberFormatUtils.formatCompactNumber(1000, Locale.US))
    }

    @Test
    fun `formatCompactNumber en-US mid thousand returns one decimal k`() {
        assertEquals("1.5k", NumberFormatUtils.formatCompactNumber(1500, Locale.US))
    }

    @Test
    fun `formatCompactNumber en-US at ten thousand returns ten k`() {
        assertEquals("10k", NumberFormatUtils.formatCompactNumber(10_000, Locale.US))
    }

    @Test
    fun `formatCompactNumber en-US at million returns one M`() {
        assertEquals("1M", NumberFormatUtils.formatCompactNumber(1_000_000, Locale.US))
    }

    @Test
    fun `formatCompactNumber zh-CN at ten thousand returns wan`() {
        assertEquals("1万", NumberFormatUtils.formatCompactNumber(10_000, Locale.SIMPLIFIED_CHINESE))
    }

    @Test
    fun `formatCompactNumber zh-CN at million returns wan`() {
        assertEquals("100万", NumberFormatUtils.formatCompactNumber(1_000_000, Locale.SIMPLIFIED_CHINESE))
    }

    @Test
    fun `formatCompactNumber ja-JP at ten thousand returns man`() {
        assertEquals("1万", NumberFormatUtils.formatCompactNumber(10_000, Locale.JAPAN))
    }

    @Test
    fun `formatCompactNumber ko-KR at ten thousand returns man`() {
        assertEquals("1만", NumberFormatUtils.formatCompactNumber(10_000, Locale.KOREA))
    }

    @Test
    fun `formatCompactNumber float value returns compact string`() {
        assertEquals("10.5k", NumberFormatUtils.formatCompactNumber(10_499.5f, Locale.US))
    }
}
