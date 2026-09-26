package com.yellowskippergames.vagudle

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import kotlin.math.sqrt

private const val COMPACT_REFERENCE_SIZE = 40f
private const val EXPANDED_REFERENCE_WIDTH = 180f
private const val ACHIEVEMENTS_EXPANDED_MIN_ASPECT = 1.6f
private val ACHIEVEMENTS_EXPANDED_MIN_WIDTH = 150.dp

class AchievementsWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val initialViewState = loadAchievementsWidgetViewState(context)
        provideContent {
            val viewState by achievementsWidgetViewStateUpdates(context).collectAsState(initial = initialViewState)
            AchievementsWidgetContent(context, viewState)
        }
    }
}

@Composable
private fun AchievementsWidgetContent(
    context: Context,
    viewState: AchievementsWidgetViewState,
) {
    val reportedSize = LocalSize.current
    val edgeSafeMargin = 3.dp
    val isExpanded =
        reportedSize.width >= ACHIEVEMENTS_EXPANDED_MIN_WIDTH &&
            reportedSize.width.value / reportedSize.height.value >= ACHIEVEMENTS_EXPANDED_MIN_ASPECT
    val size =
        DpSize(
            width = (reportedSize.width - edgeSafeMargin * 2).coerceAtLeast(1.dp),
            height = (reportedSize.height - edgeSafeMargin * 2).coerceAtLeast(1.dp),
        )
    val scale = achievementsWidgetScale(size, isExpanded)
    val density = context.resources.displayMetrics.density
    val borderThickness = 3.dp.scaled(scale.visual)
    val innerWidth = size.width - borderThickness * 2
    val innerHeight = size.height - borderThickness * 2
    val fallbackOuterRadius = 16.dp.scaled(scale.visual).coerceAtLeast(MIN_OUTER_CORNER_RADIUS)
    val outerRadius =
        (systemWidgetCornerRadius(context, density) ?: fallbackOuterRadius)
            .coerceAtMost(minOf(size.width, size.height) / 2)
    val innerRadius = (outerRadius - borderThickness).coerceAtLeast(MIN_INNER_CORNER_RADIUS)
    val openApp = actionStartActivity(openWidgetIntent(context, WidgetKind.ACHIEVEMENTS))

    Box(
        modifier = GlanceModifier.fillMaxSize().padding(edgeSafeMargin),
        contentAlignment = Alignment.Center,
    ) {
        RoundedZoneBox(
            width = size.width,
            height = size.height,
            fillColor = GOLD_ARGB,
            topLeftRadius = outerRadius,
            topRightRadius = outerRadius,
            bottomRightRadius = outerRadius,
            bottomLeftRadius = outerRadius,
            density = density,
            modifier = GlanceModifier.clickable(openApp),
        ) {
            Box(
                modifier = GlanceModifier.fillMaxSize().padding(vertical = borderThickness),
                contentAlignment = Alignment.Center,
            ) {
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    Spacer(modifier = GlanceModifier.width(borderThickness).fillMaxHeight())
                    Box(
                        modifier = GlanceModifier.width(innerWidth).height(innerHeight),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isExpanded) {
                            AchievementsExpandedContent(
                                context,
                                viewState,
                                scale,
                                innerWidth,
                                innerHeight,
                                innerRadius,
                                density,
                            )
                        } else {
                            AchievementsCompactContent(
                                context,
                                viewState,
                                scale,
                                innerWidth,
                                innerHeight,
                                innerRadius,
                                density,
                            )
                        }
                    }
                    Spacer(modifier = GlanceModifier.width(borderThickness).fillMaxHeight())
                }
            }
        }
    }
}

private fun achievementsWidgetScale(
    size: DpSize,
    isExpanded: Boolean,
): WidgetScale =
    if (isExpanded) {
        val widthScale = size.width.value / EXPANDED_REFERENCE_WIDTH
        val heightScale = size.height.value / COMPACT_REFERENCE_SIZE
        WidgetScale(
            width = widthScale,
            height = heightScale,
            visual = minOf(sqrt(widthScale * heightScale), heightScale),
            textBoost = 1f,
        )
    } else {
        val uniformScale = minOf(size.width.value, size.height.value) / COMPACT_REFERENCE_SIZE
        WidgetScale(width = uniformScale, height = uniformScale, visual = uniformScale, textBoost = 1f)
    }
