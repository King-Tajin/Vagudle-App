package com.yellowskippergames.vagudle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

private fun roundedRectBitmap(
    widthPx: Int,
    heightPx: Int,
    fillColor: Int,
    topLeftRadius: Float,
    topRightRadius: Float,
    bottomRightRadius: Float,
    bottomLeftRadius: Float,
    strokeWidthPx: Int = 0,
    strokeColor: Int = 0,
): Bitmap {
    val drawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fillColor)
            cornerRadii =
                floatArrayOf(
                    topLeftRadius,
                    topLeftRadius,
                    topRightRadius,
                    topRightRadius,
                    bottomRightRadius,
                    bottomRightRadius,
                    bottomLeftRadius,
                    bottomLeftRadius,
                )
            if (strokeWidthPx > 0) setStroke(strokeWidthPx, strokeColor)
        }
    val safeWidth = widthPx.coerceAtLeast(1)
    val safeHeight = heightPx.coerceAtLeast(1)
    drawable.setBounds(0, 0, safeWidth, safeHeight)
    val bitmap = createBitmap(safeWidth, safeHeight)
    drawable.draw(Canvas(bitmap))
    return bitmap
}

@Composable
internal fun RoundedZoneBox(
    width: Dp,
    height: Dp,
    fillColor: Int,
    topLeftRadius: Dp,
    topRightRadius: Dp,
    bottomRightRadius: Dp,
    bottomLeftRadius: Dp,
    density: Float,
    strokeWidth: Dp = 0.dp,
    strokeColor: Int = 0,
    contentAlignment: Alignment = Alignment.Center,
    modifier: GlanceModifier = GlanceModifier,
    content: @Composable () -> Unit,
) {
    val bitmap =
        roundedRectBitmap(
            widthPx = dpToPx(width, density),
            heightPx = dpToPx(height, density),
            fillColor = fillColor,
            topLeftRadius = dpToPx(topLeftRadius, density).toFloat(),
            topRightRadius = dpToPx(topRightRadius, density).toFloat(),
            bottomRightRadius = dpToPx(bottomRightRadius, density).toFloat(),
            bottomLeftRadius = dpToPx(bottomLeftRadius, density).toFloat(),
            strokeWidthPx = dpToPx(strokeWidth, density).let { if (strokeWidth.value > 0f) it else 0 },
            strokeColor = strokeColor,
        )
    Box(
        modifier = modifier.width(width).height(height).background(ImageProvider(bitmap)),
        contentAlignment = contentAlignment,
    ) {
        content()
    }
}

@Composable
internal fun Plaque(
    context: Context,
    dailyNumber: Int,
    scale: WidgetScale,
    width: Dp,
    height: Dp,
    labelFontSize: TextUnit,
    numberFontSize: TextUnit,
) {
    val density = context.resources.displayMetrics.density
    RoundedZoneBox(
        width = width,
        height = height,
        fillColor = PLAQUE_FILL_ARGB,
        topLeftRadius = 6.dp.scaled(scale.visual),
        topRightRadius = 6.dp.scaled(scale.visual),
        bottomRightRadius = 6.dp.scaled(scale.visual),
        bottomLeftRadius = 6.dp.scaled(scale.visual),
        density = density,
        strokeWidth = 2.dp.scaled(scale.visual),
        strokeColor = PLAQUE_BORDER_ARGB,
    ) {
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            Text(
                text = context.getString(R.string.widget_daily_label),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = labelFontSize.scaled(scale), color = GOLD),
                maxLines = 1,
            )
            Text(
                text = context.getString(R.string.widget_daily_number, dailyNumber),
                style =
                    TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = numberFontSize.scaled(scale),
                        color = TEXT_WHITE,
                    ),
                maxLines = 1,
            )
        }
    }
}
