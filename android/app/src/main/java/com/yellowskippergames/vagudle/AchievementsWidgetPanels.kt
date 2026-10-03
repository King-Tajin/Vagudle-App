package com.yellowskippergames.vagudle

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

private const val TITLE_CHAR_WIDTH_EM = 0.58f
private const val TITLE_MIN_SP = 5.5f
private const val TITLE_MAX_SP = 7f
private const val LABEL_SP = 4.5f
private const val SUBTITLE_SP = 5.5f
private const val MIN_FILL_DP = 4f

private data class RightPanel(
    val label: String?,
    val title: String,
    val titleColor: ColorProvider,
    val value: String? = null,
    val valueColor: ColorProvider = GOLD,
    val barFraction: Float? = null,
    val subtitle: String? = null,
)

private fun rightPanel(
    context: Context,
    viewState: AchievementsWidgetViewState,
): RightPanel {
    val data = viewState.data
    return when {
        data == null -> {
            val message = widgetMessage(context, viewState.setupFailed)
            RightPanel(
                label = null,
                title = message.title,
                titleColor = if (message.isError) NOT_PLAYED_RED else GOLD,
                subtitle = message.subtitle,
            )
        }
        data.isComplete ->
            RightPanel(
                label = context.getString(R.string.widget_achievements_complete),
                title = data.nextUpTitle,
                titleColor = GOLD,
                value = context.getString(R.string.widget_achievements_unlocked),
                valueColor = SOLVED_GREEN,
                barFraction = 1f,
            )
        data.hasCounter -> {
            val progress = data.nextUpProgress ?: 0
            val target = data.nextUpTarget ?: 1
            RightPanel(
                label = context.getString(R.string.widget_achievements_next_up),
                title = data.nextUpTitle,
                titleColor = TEXT_WHITE,
                value = context.getString(R.string.widget_achievements_fraction, progress, target),
                barFraction = progress.toFloat() / target,
            )
        }
        else ->
            RightPanel(
                label = context.getString(R.string.widget_achievements_still_locked),
                title = data.nextUpTitle,
                titleColor = TEXT_WHITE,
            )
    }
}

@Composable
internal fun AchievementsCompactContent(
    context: Context,
    viewState: AchievementsWidgetViewState,
    scale: WidgetScale,
    frame: WidgetFrame,
) {
    RoundedZoneBox(
        width = frame.innerWidth,
        height = frame.innerHeight,
        fillColor = SLATE_ARGB,
        topLeftRadius = frame.innerRadius,
        topRightRadius = frame.innerRadius,
        bottomRightRadius = frame.innerRadius,
        bottomLeftRadius = frame.innerRadius,
        density = frame.density,
        modifier = GlanceModifier.fillMaxSize(),
        flexibleWidth = true,
        flexibleHeight = true,
    ) {
        AchievementsCoin(
            context = context,
            model = coinModel(context, viewState),
            scale = minOf(scale.visual, frame.innerWidth.value / COIN_BOX_DP, frame.innerHeight.value / COIN_BOX_DP),
            density = frame.density,
        )
    }
}

@Composable
internal fun AchievementsExpandedContent(
    context: Context,
    viewState: AchievementsWidgetViewState,
    scale: WidgetScale,
    frame: WidgetFrame,
) {
    val split = widgetSplitLayout(frame.innerWidth, frame.innerHeight, scale.visual)
    Row(modifier = GlanceModifier.fillMaxSize()) {
        RoundedZoneBox(
            width = split.leftWidth,
            height = frame.innerHeight,
            modifier = GlanceModifier.fillMaxHeight(),
            flexibleHeight = true,
            fillColor = SLATE_ARGB,
            topLeftRadius = frame.innerRadius,
            topRightRadius = split.inwardRadius,
            bottomRightRadius = split.inwardRadius,
            bottomLeftRadius = frame.innerRadius,
            density = frame.density,
        ) {
            AchievementsCoin(
                context = context,
                model = coinModel(context, viewState),
                scale =
                    minOf(
                        scale.visual,
                        split.leftWidth.value / COIN_BOX_DP,
                        frame.innerHeight.value / COIN_BOX_DP,
                    ),
                density = frame.density,
            )
        }
        Spacer(modifier = GlanceModifier.width(split.gap).fillMaxHeight())
        RoundedZoneBox(
            width = split.rightWidth,
            height = frame.innerHeight,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            flexibleWidth = true,
            flexibleHeight = true,
            fillColor = NEAR_BLACK_ARGB,
            topLeftRadius = split.inwardRadius,
            topRightRadius = frame.innerRadius,
            bottomRightRadius = frame.innerRadius,
            bottomLeftRadius = split.inwardRadius,
            density = frame.density,
        ) {
            AchievementsRightZone(
                rightPanel(context, viewState),
                split.rightWidth,
                frame.innerHeight,
                scale,
                frame.density,
            )
        }
    }
}

@Composable
private fun AchievementsRightZone(
    panel: RightPanel,
    zoneWidth: Dp,
    zoneHeight: Dp,
    scale: WidgetScale,
    density: Float,
) {
    val contentWidth = zoneWidth * (105f / 121f)
    val horizontalPadding = zoneWidth * (8f / 121f)
    val valueLength = panel.value?.let { it.length + 2 } ?: 0
    val fitSp = contentWidth.value / ((panel.title.length + valueLength) * TITLE_CHAR_WIDTH_EM)
    val maxTitleSp = TITLE_MAX_SP * zoneHeight.value / COIN_BOX_DP
    val titleSize = minOf(maxTitleSp, maxOf(fitSp, TITLE_MIN_SP * scale.width))
    val unit = titleSize / TITLE_MAX_SP
    val titleSp = titleSize.sp

    Box(
        modifier = GlanceModifier.fillMaxSize().padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            if (panel.label != null) {
                Text(
                    text = panel.label,
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (LABEL_SP * unit).sp,
                            color = MUTED_GRAY,
                        ),
                    maxLines = 1,
                )
                Spacer(modifier = GlanceModifier.height((1.5f * unit).dp))
            }
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = panel.title,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = titleSp, color = panel.titleColor),
                    maxLines = 1,
                )
                if (panel.value != null) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = panel.value,
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = titleSp, color = panel.valueColor),
                        maxLines = 1,
                    )
                }
            }
            if (panel.barFraction != null) {
                Spacer(modifier = GlanceModifier.height((2.5f * unit).dp))
                ProgressBar(contentWidth, panel.barFraction, unit, density)
            }
            if (panel.subtitle != null) {
                Spacer(modifier = GlanceModifier.height((2f * unit).dp))
                Text(
                    text = panel.subtitle,
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (SUBTITLE_SP * unit).sp,
                            color = MUTED_GRAY,
                        ),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(
    width: Dp,
    fraction: Float,
    unit: Float,
    density: Float,
) {
    val barHeight = (4f * unit).dp
    val radius = (2f * unit).dp
    RoundedZoneBox(
        width = width,
        height = barHeight,
        fillColor = SLATE_ARGB,
        topLeftRadius = radius,
        topRightRadius = radius,
        bottomRightRadius = radius,
        bottomLeftRadius = radius,
        density = density,
        contentAlignment = Alignment.CenterStart,
    ) {
        if (fraction > 0f) {
            val fillWidth = maxOf(width * fraction.coerceAtMost(1f), (MIN_FILL_DP * unit).dp).coerceAtMost(width)
            RoundedZoneBox(
                width = fillWidth,
                height = barHeight,
                fillColor = GOLD_ARGB,
                topLeftRadius = radius,
                topRightRadius = radius,
                bottomRightRadius = radius,
                bottomLeftRadius = radius,
                density = density,
            ) {}
        }
    }
}
