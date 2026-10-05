package com.yellowskippergames.vagudle.widget.daily

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.yellowskippergames.vagudle.widget.core.WidgetKind
import com.yellowskippergames.vagudle.widget.core.widgetMessage
import com.yellowskippergames.vagudle.widget.sync.openWidgetIntent
import com.yellowskippergames.vagudle.widget.ui.COMPACT_TEXT_BOOST
import com.yellowskippergames.vagudle.widget.ui.EXPANDED_TEXT_BOOST
import com.yellowskippergames.vagudle.widget.ui.GOLD
import com.yellowskippergames.vagudle.widget.ui.NEAR_BLACK_ARGB
import com.yellowskippergames.vagudle.widget.ui.RoundedZoneBox
import com.yellowskippergames.vagudle.widget.ui.WidgetFrame
import com.yellowskippergames.vagudle.widget.ui.WidgetFrameBox
import com.yellowskippergames.vagudle.widget.ui.WidgetScale
import com.yellowskippergames.vagudle.widget.ui.edgeSafeSize
import com.yellowskippergames.vagudle.widget.ui.fitTextSp
import com.yellowskippergames.vagudle.widget.ui.widgetFrame
import kotlin.math.sqrt

internal val SIZE_COMPACT = DpSize(180.dp, 40.dp)
internal val SIZE_EXPANDED = DpSize(180.dp, 110.dp)
internal val COMPACT_ASPECT = SIZE_COMPACT.height.value / SIZE_COMPACT.width.value
internal val EXPANDED_ASPECT = SIZE_EXPANDED.height.value / SIZE_EXPANDED.width.value
internal val EXPANDED_ASPECT_THRESHOLD = (COMPACT_ASPECT + EXPANDED_ASPECT) / 2f

class DailyWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val initialViewState = loadDailyWidgetViewState(context)
        provideContent {
            val viewState by dailyWidgetViewStateUpdates(context).collectAsState(initial = initialViewState)
            DailyWidgetContent(context, viewState)
        }
    }
}

internal fun openDailyIntent(context: Context): Intent = openWidgetIntent(context, WidgetKind.DAILY)

@Composable
private fun DailyWidgetContent(
    context: Context,
    viewState: DailyWidgetViewState,
) {
    val data = viewState.data
    val size = edgeSafeSize(LocalSize.current)
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
    val frame = widgetFrame(context, size, scale.visual, if (isExpanded) 15.dp else 16.dp)

    WidgetFrameBox(frame, actionStartActivity(openDailyIntent(context))) {
        if (data == null) {
            EmptyState(context, frame, viewState.setupFailed)
        } else {
            val isFresh = data.date == currentDailyDateUtc()
            if (isExpanded) {
                ExpandedPanelContent(
                    context = context,
                    data = data,
                    isFresh = isFresh,
                    scale = scale,
                    innerWidth = frame.innerWidth,
                    innerHeight = frame.innerHeight,
                    outerCornerRadius = frame.innerRadius,
                )
            } else {
                CompactPanelContent(
                    context = context,
                    data = data,
                    isFresh = isFresh,
                    scale = scale,
                    innerWidth = frame.innerWidth,
                    innerHeight = frame.innerHeight,
                    outerCornerRadius = frame.innerRadius,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    context: Context,
    frame: WidgetFrame,
    setupFailed: Boolean,
) {
    val width = frame.innerWidth
    val height = frame.innerHeight
    val message = widgetMessage(context, setupFailed)
    val titleFontSize = fitTextSp(width, height, message.title.length, heightFraction = 0.32f, minSp = 6f, maxSp = 22f)
    val subtitleFontSize =
        fitTextSp(width, height, message.subtitle.length, heightFraction = 0.2f, minSp = 5f, maxSp = 16f)

    RoundedZoneBox(
        width = width,
        height = height,
        fillColor = NEAR_BLACK_ARGB,
        topLeftRadius = frame.innerRadius,
        topRightRadius = frame.innerRadius,
        bottomRightRadius = frame.innerRadius,
        bottomLeftRadius = frame.innerRadius,
        density = frame.density,
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier.fillMaxSize(),
        flexibleWidth = true,
        flexibleHeight = true,
    ) {
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            Text(
                text = message.title,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = titleFontSize, color = GOLD),
                maxLines = 1,
            )
            Spacer(modifier = GlanceModifier.height((height.value * 0.06f).dp.coerceAtLeast(2.dp)))
            Text(
                text = message.subtitle,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = subtitleFontSize, color = GOLD),
                maxLines = 1,
            )
        }
    }
}
