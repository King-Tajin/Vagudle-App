package com.yellowskippergames.vagudle.widget.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width

internal val EDGE_SAFE_MARGIN = 3.dp

internal data class WidgetFrame(
    val size: DpSize,
    val density: Float,
    val borderThickness: Dp,
    val innerWidth: Dp,
    val innerHeight: Dp,
    val outerRadius: Dp,
    val innerRadius: Dp,
)

internal data class WidgetSplitLayout(
    val gap: Dp,
    val leftWidth: Dp,
    val rightWidth: Dp,
    val inwardRadius: Dp,
)

internal fun edgeSafeSize(reportedSize: DpSize): DpSize =
    DpSize(
        width = (reportedSize.width - EDGE_SAFE_MARGIN * 2).coerceAtLeast(1.dp),
        height = (reportedSize.height - EDGE_SAFE_MARGIN * 2).coerceAtLeast(1.dp),
    )

internal fun widgetFrame(
    context: Context,
    size: DpSize,
    visualScale: Float,
    fallbackOuterRadius: Dp = 16.dp,
): WidgetFrame {
    val density = context.resources.displayMetrics.density
    val borderThickness = 3.dp.scaled(visualScale)
    val scaledFallbackRadius = fallbackOuterRadius.scaled(visualScale).coerceAtLeast(MIN_OUTER_CORNER_RADIUS)
    val outerRadius =
        (systemWidgetCornerRadius(context, density) ?: scaledFallbackRadius)
            .coerceAtLeast(borderThickness + MIN_INNER_CORNER_RADIUS)
            .coerceAtMost(minOf(size.width, size.height) / 2)
    return WidgetFrame(
        size = size,
        density = density,
        borderThickness = borderThickness,
        innerWidth = size.width - borderThickness * 2,
        innerHeight = size.height - borderThickness * 2,
        outerRadius = outerRadius,
        innerRadius = (outerRadius - borderThickness).coerceAtLeast(MIN_INNER_CORNER_RADIUS),
    )
}

internal fun widgetSplitLayout(
    innerWidth: Dp,
    innerHeight: Dp,
    visualScale: Float,
): WidgetSplitLayout {
    val gap = 3.dp.scaled(visualScale)
    val leftWidth = minOf(innerHeight * (50f / 34f), innerWidth * 0.36f)
    return WidgetSplitLayout(
        gap = gap,
        leftWidth = leftWidth,
        rightWidth = innerWidth - leftWidth - gap,
        inwardRadius = 6.dp.scaled(visualScale),
    )
}

@Composable
internal fun WidgetFrameBox(
    frame: WidgetFrame,
    onClick: Action,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = GlanceModifier.fillMaxSize().padding(EDGE_SAFE_MARGIN),
        contentAlignment = Alignment.Center,
    ) {
        RoundedZoneBox(
            width = frame.size.width,
            height = frame.size.height,
            fillColor = GOLD_ARGB,
            topLeftRadius = frame.outerRadius,
            topRightRadius = frame.outerRadius,
            bottomRightRadius = frame.outerRadius,
            bottomLeftRadius = frame.outerRadius,
            density = frame.density,
            modifier = GlanceModifier.fillMaxSize().clickable(onClick),
            flexibleWidth = true,
            flexibleHeight = true,
        ) {
            Box(
                modifier = GlanceModifier.fillMaxSize().padding(vertical = frame.borderThickness),
                contentAlignment = Alignment.Center,
            ) {
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    Spacer(modifier = GlanceModifier.width(frame.borderThickness).fillMaxHeight())
                    Box(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        content()
                    }
                    Spacer(modifier = GlanceModifier.width(frame.borderThickness).fillMaxHeight())
                }
            }
        }
    }
}
