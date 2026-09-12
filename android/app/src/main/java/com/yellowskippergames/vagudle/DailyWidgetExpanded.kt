package com.yellowskippergames.vagudle

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

@Composable
internal fun ExpandedPanelContent(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
    innerWidth: Dp,
    innerHeight: Dp,
) {
    val density = context.resources.displayMetrics.density
    val leftZoneWidth = innerWidth * (54f / 174f)
    val gapWidth = innerWidth * (3f / 174f)
    val rightZoneWidth = innerWidth - leftZoneWidth - gapWidth
    val sidePadding = 10.dp.scaled(scale.visual)
    Row(modifier = GlanceModifier.fillMaxSize()) {
        RoundedZoneBox(
            width = leftZoneWidth,
            height = innerHeight,
            fillColor = SLATE_ARGB,
            topLeftRadius = 12.dp.scaled(scale.visual),
            topRightRadius = 8.dp.scaled(scale.visual),
            bottomRightRadius = 8.dp.scaled(scale.visual),
            bottomLeftRadius = 12.dp.scaled(scale.visual),
            density = density,
        ) {
            Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                Plaque(
                    context = context,
                    dailyNumber = data.dailyNumber,
                    scale = scale,
                    width = leftZoneWidth * (38f / 54f),
                    height = innerHeight * (25f / 104f),
                    labelFontSize = 6.sp,
                    numberFontSize = 10.sp,
                )
                Spacer(modifier = GlanceModifier.height(6.dp.scaled(scale.visual)))
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_flame),
                        contentDescription = null,
                        modifier =
                            GlanceModifier
                                .width(
                                    6.29f.dp.scaled(scale.visual),
                                ).height(11.dp.scaled(scale.visual)),
                    )
                    Spacer(modifier = GlanceModifier.width(3.dp.scaled(scale.visual)))
                    Text(
                        text = data.currentStreak.toString(),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp.scaled(scale), color = GOLD),
                        maxLines = 1,
                    )
                }
                Text(
                    text = context.getString(R.string.widget_streak_label),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 6.sp.scaled(scale), color = GOLD),
                    maxLines = 1,
                )
                Spacer(modifier = GlanceModifier.height(6.dp.scaled(scale.visual)))
                Text(
                    text = context.getString(R.string.widget_best_streak, data.bestStreak),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 8.sp.scaled(scale), color = MUTED_GRAY),
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = GlanceModifier.width(gapWidth).fillMaxHeight())
        RoundedZoneBox(
            width = rightZoneWidth,
            height = innerHeight,
            fillColor = NEAR_BLACK_ARGB,
            topLeftRadius = 8.dp.scaled(scale.visual),
            topRightRadius = 12.dp.scaled(scale.visual),
            bottomRightRadius = 12.dp.scaled(scale.visual),
            bottomLeftRadius = 8.dp.scaled(scale.visual),
            density = density,
        ) {
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .padding(horizontal = sidePadding, vertical = 8.dp.scaled(scale.visual)),
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    ExpandedStatusLine(context, data, isFresh, scale)
                    Spacer(modifier = GlanceModifier.height(6.dp.scaled(scale.visual)))
                    ExpandedRankRow(context, data, scale)
                    Spacer(modifier = GlanceModifier.height(4.dp.scaled(scale.visual)))
                    Text(
                        text =
                            context.getString(
                                R.string.widget_daily_info_short,
                                data.wordLength,
                                context.getString(
                                    if (data.hardMode) R.string.widget_mode_hard else R.string.widget_mode_normal,
                                ),
                            ),
                        style =
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp.scaled(scale),
                                color = MUTED_GRAY,
                            ),
                        maxLines = 1,
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp.scaled(scale.visual)))
                    PlayButton(
                        context = context,
                        data = data,
                        isFresh = isFresh,
                        scale = scale,
                        buttonWidth = rightZoneWidth - sidePadding * 2,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedStatusLine(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
) {
    val status = statusInfo(context, data, isFresh, scale, baseFontSize = 12.sp, uniformSize = true)
    Text(
        text = status.text,
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = status.fontSize, color = status.color),
        maxLines = 1,
    )
}

@Composable
private fun ExpandedRankRow(
    context: Context,
    data: DailyWidgetData,
    scale: WidgetScale,
) {
    val (rankText, rankFontSize) = rankInfo(context, data.rank, scale, baseFontSize = 10.sp, highRankFontSize = 8.sp)
    Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_trophy),
            contentDescription = null,
            modifier = GlanceModifier.width(9.5f.dp.scaled(scale.visual)).height(11.dp.scaled(scale.visual)),
        )
        Spacer(modifier = GlanceModifier.width(4.dp.scaled(scale.visual)))
        Text(
            text = rankText,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = rankFontSize, color = GOLD),
            maxLines = 1,
        )
    }
}

private fun playButtonLabel(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
): String =
    when {
        isFresh && data.hasPlayedToday -> context.getString(R.string.widget_play_normal_mode)
        isFresh && data.inProgress -> context.getString(R.string.widget_resume)
        else -> context.getString(R.string.widget_play_daily)
    }

private fun playButtonFontSize(
    labelLength: Int,
    buttonWidth: Dp,
    scale: WidgetScale,
): TextUnit {
    val iconAndGapOverheadDp = 24f
    val pillPaddingDp = 16f
    val referenceButtonWidthDp = buttonWidth.value / scale.width
    val availableTextWidthDp = referenceButtonWidthDp - iconAndGapOverheadDp - pillPaddingDp
    val rawSize = availableTextWidthDp / (labelLength * 0.56f)
    return rawSize.coerceIn(6f, 9f).scaledSp(scale)
}

@Composable
private fun PlayButton(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
    buttonWidth: Dp,
) {
    val density = context.resources.displayMetrics.density
    val buttonHeight = 16.dp.scaled(scale.visual)
    val label = playButtonLabel(context, data, isFresh)
    val fontSize = playButtonFontSize(label.length, buttonWidth, scale)
    val cornerRadius = buttonHeight / 2
    RoundedZoneBox(
        width = buttonWidth,
        height = buttonHeight,
        fillColor = GOLD_ARGB,
        topLeftRadius = cornerRadius,
        topRightRadius = cornerRadius,
        bottomRightRadius = cornerRadius,
        bottomLeftRadius = cornerRadius,
        density = density,
        modifier = GlanceModifier.clickable(actionStartActivity(openDailyIntent(context))),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_play),
                contentDescription = null,
                modifier = GlanceModifier.width(6.dp.scaled(scale.visual)).height(7.5f.dp.scaled(scale.visual)),
            )
            Spacer(modifier = GlanceModifier.width(4.dp.scaled(scale.visual)))
            Text(
                text = label,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize, color = NAVY),
                maxLines = 1,
            )
        }
    }
}
