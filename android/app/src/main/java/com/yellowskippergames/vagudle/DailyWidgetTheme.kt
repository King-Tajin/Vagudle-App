package com.yellowskippergames.vagudle

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.color.ColorProvider
import kotlin.math.roundToInt

internal val SIZE_COMPACT = DpSize(180.dp, 40.dp)
internal val SIZE_EXPANDED = DpSize(180.dp, 110.dp)
internal val COMPACT_ASPECT = SIZE_COMPACT.height.value / SIZE_COMPACT.width.value
internal val EXPANDED_ASPECT = SIZE_EXPANDED.height.value / SIZE_EXPANDED.width.value
internal val EXPANDED_ASPECT_THRESHOLD = (COMPACT_ASPECT + EXPANDED_ASPECT) / 2f
internal const val DEEP_LINK_URL = "https://vagudle.king-tajin.dev/daily"

private fun fixedColor(color: Color) = ColorProvider(day = color, night = color)

internal const val SLATE_ARGB = 0xFF45556C.toInt()
internal const val NEAR_BLACK_ARGB = 0xFF0D0D1B.toInt()
internal const val PLAQUE_FILL_ARGB = 0xFF0A0014.toInt()
internal const val PLAQUE_BORDER_ARGB = 0xFF4E00A7.toInt()
internal const val GOLD_ARGB = 0xFFFFD700.toInt()

internal val MIN_OUTER_CORNER_RADIUS = 8.dp
internal val MIN_INNER_CORNER_RADIUS = 6.dp

internal val GOLD = fixedColor(Color(0xFFFFD700))
internal val TEXT_WHITE = fixedColor(Color(0xFFFFFFFF))
internal val MUTED_GRAY = fixedColor(Color(0xFF8A8A8A))
internal val SOLVED_GREEN = fixedColor(Color(0xFF4A7C3F))
internal val IN_PROGRESS_ORANGE = fixedColor(Color(0xFFFF8C00))
internal val NOT_PLAYED_RED = fixedColor(Color(0xFFC41E3A))
internal val NAVY = fixedColor(Color(0xFF0D1322))

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
