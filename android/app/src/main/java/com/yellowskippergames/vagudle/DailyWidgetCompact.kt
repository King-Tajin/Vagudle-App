package com.yellowskippergames.vagudle

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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

@Composable
internal fun CompactPanelContent(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
    innerWidth: Dp,
    innerHeight: Dp,
) {
    val density = context.resources.displayMetrics.density
    val leftZoneWidth = innerWidth * (50f / 174f)
    val gapWidth = innerWidth * (3f / 174f)
    val rightZoneWidth = innerWidth - leftZoneWidth - gapWidth
    Row(modifier = GlanceModifier.fillMaxSize()) {
        RoundedZoneBox(
            width = leftZoneWidth,
            height = innerHeight,
            fillColor = SLATE_ARGB,
            topLeftRadius = 13.dp.scaled(scale.visual),
            topRightRadius = 6.dp.scaled(scale.visual),
            bottomRightRadius = 6.dp.scaled(scale.visual),
            bottomLeftRadius = 13.dp.scaled(scale.visual),
            density = density,
        ) {
            Plaque(
                context = context,
                dailyNumber = data.dailyNumber,
                scale = scale,
                width = leftZoneWidth * (40f / 50f),
                height = innerHeight * (28f / 34f),
                labelFontSize = 4.sp,
                numberFontSize = 9.sp,
            )
        }
        Spacer(modifier = GlanceModifier.width(gapWidth).fillMaxHeight())
        RoundedZoneBox(
            width = rightZoneWidth,
            height = innerHeight,
            fillColor = NEAR_BLACK_ARGB,
            topLeftRadius = 6.dp.scaled(scale.visual),
            topRightRadius = 13.dp.scaled(scale.visual),
            bottomRightRadius = 13.dp.scaled(scale.visual),
            bottomLeftRadius = 6.dp.scaled(scale.visual),
            density = density,
        ) {
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp.scaled(scale.visual)),
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    StreakRow(context, data, scale)
                    Spacer(modifier = GlanceModifier.height(4.dp.scaled(scale.visual)))
                    StatusRow(context, data, isFresh, scale)
                }
            }
        }
    }
}

@Composable
private fun StreakRow(
    context: Context,
    data: DailyWidgetData,
    scale: WidgetScale,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_flame),
            contentDescription = null,
            modifier = GlanceModifier.width(4.67f.dp.scaled(scale.visual)).height(8.16f.dp.scaled(scale.visual)),
        )
        Spacer(modifier = GlanceModifier.width(2.dp.scaled(scale.visual)))
        Text(
            text = data.currentStreak.toString(),
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 7.sp.scaled(scale), color = GOLD),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            text = context.getString(R.string.widget_best_streak, data.bestStreak),
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 5.5f.sp.scaled(scale), color = MUTED_GRAY),
            maxLines = 1,
        )
    }
}

@Composable
private fun StatusRow(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
) {
    val status = statusInfo(context, data, isFresh, scale)
    val (rankText, rankFontSize) = rankInfo(context, data.rank, scale)
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = status.text,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = status.fontSize, color = status.color),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Image(
            provider = ImageProvider(R.drawable.ic_widget_trophy),
            contentDescription = null,
            modifier = GlanceModifier.width(5.6f.dp.scaled(scale.visual)).height(6.48f.dp.scaled(scale.visual)),
        )
        Spacer(modifier = GlanceModifier.width(2.dp.scaled(scale.visual)))
        Text(
            text = rankText,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = rankFontSize, color = GOLD),
            maxLines = 1,
        )
    }
}
