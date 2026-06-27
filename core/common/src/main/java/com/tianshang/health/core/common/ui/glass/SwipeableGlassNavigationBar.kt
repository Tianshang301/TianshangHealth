package com.tianshang.health.core.common.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class TabItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
)

private val NavBarHeight = 68.dp
private val NavBarCornerRadius = 28.dp
private val IndicatorHeight = 44.dp
private val IndicatorCornerRadius = 22.dp

private val indicatorSpring = spring<Float>(dampingRatio = 0.65f, stiffness = 400f)
private val glassSpring = spring<Float>(dampingRatio = 0.5f, stiffness = 300f)

@Composable
fun SwipeableGlassNavigationBar(
    tabs: List<TabItem>,
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val tabCount = tabs.size.coerceAtLeast(1)
    val selectedIndex = tabs.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)

    val tabWidthPx = if (containerWidthPx > 0) containerWidthPx / tabCount else 0

    val indicatorOffset = remember { Animatable(0f) }
    val glassScaleX = remember { Animatable(1f) }
    val glassShadow = remember { Animatable(8f) }

    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    // Mutable ref so pointerInput lambda always reads the latest selectedIndex
    var latestSelectedIndex by remember { mutableIntStateOf(selectedIndex) }
    LaunchedEffect(selectedIndex) { latestSelectedIndex = selectedIndex }

    // Animate indicator to selected tab when not dragging
    LaunchedEffect(selectedIndex, tabWidthPx, isDragging) {
        if (!isDragging && tabWidthPx > 0) {
            indicatorOffset.animateTo(selectedIndex * tabWidthPx.toFloat(), indicatorSpring)
        }
    }

    // Animate glass effects when dragging ends
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            glassScaleX.animateTo(1f, glassSpring)
            glassShadow.animateTo(8f, glassSpring)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { containerWidthPx = it.width }
            .graphicsLayer {
                scaleX = glassScaleX.value
                shadowElevation = glassShadow.value
            }
            .pointerInput(tabs, tabWidthPx) {
                if (tabWidthPx <= 0) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffsetX = 0f
                        scope.launch { glassScaleX.animateTo(1.015f, glassSpring) }
                        scope.launch { glassShadow.animateTo(12f, glassSpring) }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffsetX += dragAmount
                        val baseIndex = latestSelectedIndex
                        val targetPx = (baseIndex * tabWidthPx + dragOffsetX)
                            .coerceIn(0f, (tabCount - 1) * tabWidthPx.toFloat())
                        scope.launch { indicatorOffset.snapTo(targetPx) }
                    },
                    onDragEnd = {
                        val baseIndex = latestSelectedIndex
                        val targetIndex = (baseIndex + Math.round(dragOffsetX / tabWidthPx))
                            .coerceIn(0, tabCount - 1)
                        isDragging = false
                        dragOffsetX = 0f
                        if (targetIndex != baseIndex) {
                            onTabSelected(tabs[targetIndex].route)
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffsetX = 0f
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .glass(
                    variant = GlassVariant.Regular,
                    cornerRadius = NavBarCornerRadius,
                    elevation = 0.dp,
                    isDark = isDark
                )
                .height(NavBarHeight)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val tabWidthDp = with(density) {
                if (containerWidthPx > 0) (containerWidthPx / tabCount).toDp() else 0.dp
            }

            // Indicator
            Box(
                modifier = Modifier
                    .offset { IntOffset(indicatorOffset.value.toInt(), 0) }
                    .width(tabWidthDp)
                    .height(IndicatorHeight)
                    .clip(RoundedCornerShape(IndicatorCornerRadius))
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
            )

            // Tab items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    TabItemView(
                        tab = tab,
                        isSelected = tab.route == selectedRoute,
                        onClick = { onTabSelected(tab.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItemView(
    tab: TabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconTint = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .height(IndicatorHeight)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
