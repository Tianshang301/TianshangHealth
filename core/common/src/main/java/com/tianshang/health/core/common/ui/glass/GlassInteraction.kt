package com.tianshang.health.core.common.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.glassInteraction(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    glowColor: Color = Color.White,
): Pair<Modifier, MutableInteractionSource> {
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "glass_scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.15f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "glass_glow"
    )

    val modifiedModifier = this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawBehind {
            if (glowAlpha > 0f) {
                drawCircle(
                    color = glowColor.copy(alpha = glowAlpha),
                    radius = size.maxDimension * 0.6f,
                    center = center
                )
            }
        }

    return Pair(modifiedModifier, interactionSource)
}
