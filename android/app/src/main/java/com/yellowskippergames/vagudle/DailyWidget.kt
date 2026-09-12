package com.yellowskippergames.vagudle

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlin.math.sqrt

class DailyWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val data = loadDailyWidgetData(context)
        provideContent {
            DailyWidgetContent(context, data)
        }
    }
}

internal fun openDailyIntent(context: Context): Intent =
    Intent(Intent.ACTION_VIEW, DEEP_LINK_URL.toUri()).apply {
        setPackage(context.packageName)
    }

@Composable
private fun DailyWidgetContent(
    context: Context,
    data: DailyWidgetData?,
) {
    val reportedSize = LocalSize.current
    val edgeSafeMargin = 3.dp
    val size =
        DpSize(
            width = (reportedSize.width - edgeSafeMargin * 2).coerceAtLeast(1.dp),
            height = (reportedSize.height - edgeSafeMargin * 2).coerceAtLeast(1.dp),
        )
    val isExpanded = (size.height.value / size.width.value) >= EXPANDED_ASPECT_THRESHOLD
    val reference = if (isExpanded) SIZE_EXPANDED else SIZE_COMPACT
    val widthScale = size.width.value / reference.width.value
    val heightScale = size.height.value / reference.height.value
    val textBoost = if (isExpanded) EXPANDED_TEXT_BOOST else COMPACT_TEXT_BOOST
    val scale =
        WidgetScale(
            width = widthScale,
            height = heightScale,
            visual = sqrt(widthScale * heightScale),
            textBoost = textBoost,
        )
    val density = context.resources.displayMetrics.density
    val borderThickness = 3.dp.scaled(scale.visual)
    val innerWidth = size.width - borderThickness * 2
    val innerHeight = size.height - borderThickness * 2
    val fallbackOuterRadius =
        (if (isExpanded) 15.dp else 16.dp).scaled(scale.visual).coerceAtLeast(MIN_OUTER_CORNER_RADIUS)
    val outerRadius = systemWidgetCornerRadius(context, density) ?: fallbackOuterRadius
    val innerRadius = (outerRadius - borderThickness).coerceAtLeast(MIN_INNER_CORNER_RADIUS)

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
            modifier = GlanceModifier.clickable(actionStartActivity(openDailyIntent(context))),
        ) {
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .padding(horizontal = borderThickness, vertical = borderThickness),
                contentAlignment = Alignment.Center,
            ) {
                if (data == null) {
                    EmptyState(context, innerWidth, innerHeight, innerRadius, density)
                } else {
                    val isFresh = data.date == currentDailyDateUtc()
                    if (isExpanded) {
                        ExpandedPanelContent(context, data, isFresh, scale, innerWidth, innerHeight)
                    } else {
                        CompactPanelContent(context, data, isFresh, scale, innerWidth, innerHeight)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    context: Context,
    width: Dp,
    height: Dp,
    cornerRadius: Dp,
    density: Float,
) {
    val titleText = context.getString(R.string.widget_empty_state)
    val subtitleText = context.getString(R.string.widget_empty_state_subtitle)
    val titleFontSize = fitTextSp(width, height, titleText.length, heightFraction = 0.32f, minSp = 6f, maxSp = 22f)
    val subtitleFontSize =
        fitTextSp(width, height, subtitleText.length, heightFraction = 0.2f, minSp = 5f, maxSp = 16f)

    RoundedZoneBox(
        width = width,
        height = height,
        fillColor = NEAR_BLACK_ARGB,
        topLeftRadius = cornerRadius,
        topRightRadius = cornerRadius,
        bottomRightRadius = cornerRadius,
        bottomLeftRadius = cornerRadius,
        density = density,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            Text(
                text = titleText,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = titleFontSize, color = GOLD),
                maxLines = 1,
            )
            Spacer(modifier = GlanceModifier.height((height.value * 0.06f).dp.coerceAtLeast(2.dp)))
            Text(
                text = subtitleText,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = subtitleFontSize, color = GOLD),
                maxLines = 1,
            )
        }
    }
}
