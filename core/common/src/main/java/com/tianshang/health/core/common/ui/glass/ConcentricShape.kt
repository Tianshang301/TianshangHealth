package com.tianshang.health.core.common.ui.glass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class ConcentricShape(
    private val parentRadius: Dp,
    private val padding: Dp = 12.dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val childRadiusDp = (parentRadius - padding).coerceAtLeast(0.dp)
        val childRadiusPx = with(density) { childRadiusDp.toPx() }
        val cornerShape = RoundedCornerShape(childRadiusPx)
        return cornerShape.createOutline(size, layoutDirection, density)
    }
}

fun concentricShapeOf(parentRadius: Dp, padding: Dp = 12.dp): ConcentricShape {
    return ConcentricShape(parentRadius, padding)
}

fun concentricShapeOf(parentRadius: Float, padding: Float = 12f): Shape {
    return RoundedCornerShape((parentRadius - padding).coerceAtLeast(0f))
}
