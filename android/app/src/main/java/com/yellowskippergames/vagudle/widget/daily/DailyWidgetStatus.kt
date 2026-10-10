package com.yellowskippergames.vagudle.widget.daily

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.unit.ColorProvider
import com.yellowskippergames.vagudle.R
import com.yellowskippergames.vagudle.widget.ui.IN_PROGRESS_ORANGE
import com.yellowskippergames.vagudle.widget.ui.NOT_PLAYED_RED
import com.yellowskippergames.vagudle.widget.ui.SOLVED_GREEN
import com.yellowskippergames.vagudle.widget.ui.WidgetScale
import com.yellowskippergames.vagudle.widget.ui.scaled
import kotlin.math.max

private const val MIN_FIT_FRACTION = 0.5f
private const val FIT_SAFETY = 0.95f

internal data class StatusInfo(
    val text: String,
    val color: ColorProvider,
    val fontSize: TextUnit,
)

private fun boldPaint(
    context: Context,
    fontSize: TextUnit,
): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
        textSize =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                fontSize.value,
                context.resources.displayMetrics,
            )
    }

internal fun measureTextWidth(
    context: Context,
    text: String,
    fontSize: TextUnit,
): Dp = (boldPaint(context, fontSize).measureText(text) / context.resources.displayMetrics.density).dp

private fun fitFontSize(
    context: Context,
    text: String,
    fontSize: TextUnit,
    maxWidth: Dp,
): TextUnit {
    val width = measureTextWidth(context, text, fontSize).value
    val available = maxWidth.value * FIT_SAFETY
    if (width <= 0f || width <= available) return fontSize
    return (fontSize.value * max(MIN_FIT_FRACTION, available / width)).sp
}

internal fun statusInfo(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
    baseFontSize: TextUnit = 6.sp,
    uniformSize: Boolean = false,
    maxWidth: Dp? = null,
): StatusInfo {
    val info = rawStatusInfo(context, data, isFresh, scale, baseFontSize, uniformSize)
    if (maxWidth == null) return info
    return info.copy(fontSize = fitFontSize(context, info.text, info.fontSize, maxWidth))
}

private fun rawStatusInfo(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
    baseFontSize: TextUnit,
    uniformSize: Boolean,
): StatusInfo {
    val notPlayedFontSize = if (uniformSize) baseFontSize else (baseFontSize.value * (5.5f / 6f)).sp
    if (isFresh && data.hasPlayedToday) {
        return if (data.wonToday == true) {
            val count = data.guessCount ?: 0
            StatusInfo(
                text = context.resources.getQuantityString(R.plurals.widget_solved_in_guesses, count, count),
                color = SOLVED_GREEN,
                fontSize = baseFontSize.scaled(scale),
            )
        } else {
            StatusInfo(context.getString(R.string.widget_game_lost), NOT_PLAYED_RED, baseFontSize.scaled(scale))
        }
    }
    if (isFresh && data.inProgress) {
        return StatusInfo(
            context.getString(R.string.widget_in_progress),
            IN_PROGRESS_ORANGE,
            baseFontSize.scaled(scale),
        )
    }
    return StatusInfo(
        context.getString(R.string.widget_not_played_yet),
        NOT_PLAYED_RED,
        notPlayedFontSize.scaled(scale),
    )
}

internal fun rankInfo(
    context: Context,
    rank: DailyWidgetRank,
    scale: WidgetScale,
    baseFontSize: TextUnit = 5.5f.sp,
    highRankFontSize: TextUnit = 4.5f.sp,
): Pair<String, TextUnit> =
    when (rank.status) {
        DailyWidgetRankStatus.RANKED -> {
            val position = rank.rank ?: 0
            val fontSize = if (position >= 1000) highRankFontSize else baseFontSize
            context.getString(R.string.widget_rank_number, position) to fontSize.scaled(scale)
        }
        DailyWidgetRankStatus.NO_USERNAME,
        DailyWidgetRankStatus.GUEST,
        -> "???" to baseFontSize.scaled(scale)
    }
