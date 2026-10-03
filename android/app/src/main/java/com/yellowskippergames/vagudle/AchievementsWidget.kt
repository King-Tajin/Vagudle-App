package com.yellowskippergames.vagudle

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
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

private fun openAchievementsIntent(
    context: Context,
    achievementId: String?,
): Intent =
    openWidgetIntent(
        context,
        WidgetKind.ACHIEVEMENTS,
        WidgetKind.ACHIEVEMENTS.deepLinkUrl + Uri.encode(achievementId.orEmpty()),
    )

@Composable
private fun AchievementsWidgetContent(
    context: Context,
    viewState: AchievementsWidgetViewState,
) {
    val reportedSize = LocalSize.current
    val isExpanded =
        reportedSize.width >= ACHIEVEMENTS_EXPANDED_MIN_WIDTH &&
            reportedSize.width.value / reportedSize.height.value >= ACHIEVEMENTS_EXPANDED_MIN_ASPECT
    val size = edgeSafeSize(reportedSize)
    val scale = achievementsWidgetScale(size, isExpanded)
    val frame = widgetFrame(context, size, scale.visual)
    val openApp = actionStartActivity(openAchievementsIntent(context, viewState.data?.nextUpId))

    WidgetFrameBox(frame, openApp) {
        if (isExpanded) {
            AchievementsExpandedContent(context, viewState, scale, frame)
        } else {
            AchievementsCompactContent(context, viewState, scale, frame)
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
