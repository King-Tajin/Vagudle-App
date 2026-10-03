package com.yellowskippergames.vagudle

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.color.ColorProvider
import kotlin.math.roundToInt

private fun fixedColor(argb: Int) = ColorProvider(day = Color(argb), night = Color(argb))

internal const val SLATE_ARGB = 0xFF45556C.toInt()
internal const val NEAR_BLACK_ARGB = 0xFF0D0D1B.toInt()
internal const val PLAQUE_FILL_ARGB = 0xFF0A0014.toInt()
internal const val PLAQUE_BORDER_ARGB = 0xFF4E00A7.toInt()
internal const val GOLD_ARGB = 0xFFFFD700.toInt()
internal const val TEXT_WHITE_ARGB = 0xFFFFFFFF.toInt()
internal const val MUTED_GRAY_ARGB = 0xFF8A8A8A.toInt()
internal const val SOLVED_GREEN_ARGB = 0xFF4A7C3F.toInt()
internal const val NOT_PLAYED_RED_ARGB = 0xFFC41E3A.toInt()
internal const val IN_PROGRESS_ORANGE_ARGB = 0xFFFF8C00.toInt()
internal const val NAVY_ARGB = 0xFF0D1322.toInt()

internal val MIN_OUTER_CORNER_RADIUS = 8.dp
internal val MIN_INNER_CORNER_RADIUS = 6.dp

internal val GOLD = fixedColor(GOLD_ARGB)
internal val TEXT_WHITE = fixedColor(TEXT_WHITE_ARGB)
internal val MUTED_GRAY = fixedColor(MUTED_GRAY_ARGB)
internal val SOLVED_GREEN = fixedColor(SOLVED_GREEN_ARGB)
internal val IN_PROGRESS_ORANGE = fixedColor(IN_PROGRESS_ORANGE_ARGB)
internal val NOT_PLAYED_RED = fixedColor(NOT_PLAYED_RED_ARGB)
internal val NAVY = fixedColor(NAVY_ARGB)

internal data class WidgetScale(
    val width: Float,
    val height: Float,
    val visual: Float,
    val textBoost: Float,
)

internal const val COMPACT_TEXT_BOOST = 1.35f
internal const val EXPANDED_TEXT_BOOST = 1f

internal fun Dp.scaled(scale: Float): Dp = (this.value * scale).dp

internal fun TextUnit.scaled(scale: WidgetScale): TextUnit = (this.value * scale.visual * scale.textBoost).sp

internal fun Float.scaledSp(scale: WidgetScale): TextUnit = (this * scale.visual * scale.textBoost).sp

internal fun dpToPx(
    dp: Dp,
    density: Float,
): Int = (dp.value * density).roundToInt().coerceAtLeast(1)

internal fun systemWidgetCornerRadius(
    context: Context,
    density: Float,
): Dp? {
    if (android.os.Build.VERSION.SDK_INT < 31) return null
    return try {
        val radiusPx = context.resources.getDimension(android.R.dimen.system_app_widget_background_radius)
        (radiusPx / density).dp
    } catch (_: android.content.res.Resources.NotFoundException) {
        null
    }
}

internal fun fitTextSp(
    width: Dp,
    height: Dp,
    textLength: Int,
    heightFraction: Float,
    minSp: Float,
    maxSp: Float,
): TextUnit {
    val heightBound = height.value * heightFraction
    val widthBound = (width.value * 0.9f) / (textLength * 0.52f)
    return minOf(heightBound, widthBound).coerceIn(minSp, maxSp).sp
}
