package com.yellowskippergames.vagudle

import android.content.Context
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.glance.unit.ColorProvider

internal data class StatusInfo(
    val text: String,
    val color: ColorProvider,
    val fontSize: TextUnit,
)

internal fun statusInfo(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
    baseFontSize: TextUnit = 6.sp,
    uniformSize: Boolean = false,
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
