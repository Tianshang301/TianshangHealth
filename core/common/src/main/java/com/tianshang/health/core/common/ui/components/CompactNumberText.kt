package com.tianshang.health.core.common.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.tianshang.health.core.common.util.NumberFormatUtils
import java.util.Locale

@Composable
fun CompactNumberText(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    locale: Locale = Locale.getDefault()
) {
    var showExact by remember { mutableStateOf(false) }
    val text = if (showExact) {
        NumberFormatUtils.formatExactNumber(value, locale)
    } else {
        NumberFormatUtils.formatCompactNumber(value, locale)
    }

    Text(
        text = text,
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier.clickable(
            onClickLabel = "Toggle exact number",
            onClick = { showExact = !showExact }
        )
    )
}

@Composable
fun CompactNumberText(
    value: Float,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    locale: Locale = Locale.getDefault()
) {
    CompactNumberText(
        value = value.toInt(),
        modifier = modifier,
        style = style,
        locale = locale
    )
}
