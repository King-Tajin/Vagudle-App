package com.yellowskippergames.vagudle

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import kotlin.math.sqrt

private const val STATS_REFERENCE_WIDTH = 250f
private const val STATS_REFERENCE_HEIGHT = 110f

class StatsWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val initialViewState = loadStatsWidgetViewState(context)
        provideContent {
            val viewState by statsWidgetViewStateUpdates(context).collectAsState(initial = initialViewState)
            StatsWidgetContent(context, viewState)
        }
    }
}

@Composable
private fun StatsWidgetContent(
    context: Context,
    viewState: StatsWidgetViewState,
) {
    val size = edgeSafeSize(LocalSize.current)
    val widthScale = size.width.value / STATS_REFERENCE_WIDTH
    val heightScale = size.height.value / STATS_REFERENCE_HEIGHT
    val visualScale = minOf(sqrt(widthScale * heightScale), heightScale)
    val frame = widgetFrame(context, size, visualScale)
    val split = widgetSplitLayout(frame.innerWidth, frame.innerHeight, visualScale)
    val openStats = actionStartActivity(openWidgetIntent(context, WidgetKind.STATS))
    val bitmap = statsWidgetBitmap(context, viewState, frame, split, visualScale)

    Box(
        modifier = GlanceModifier.fillMaxSize().padding(EDGE_SAFE_MARGIN),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = statsContentDescription(context, viewState),
            modifier = GlanceModifier.fillMaxSize().clickable(openStats),
            contentScale = ContentScale.Fit,
        )
    }
}

private fun statsContentDescription(
    context: Context,
    viewState: StatsWidgetViewState,
): String {
    val data = viewState.data ?: return context.getString(R.string.stats_widget_label)
    return context.getString(
        R.string.widget_stats_content_description,
        data.totalGames,
        data.normal.successRate,
        data.hard.successRate,
    )
}
