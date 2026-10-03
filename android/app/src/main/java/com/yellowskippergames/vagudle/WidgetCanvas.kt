package com.yellowskippergames.vagudle

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.height
import androidx.glance.layout.width

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
    flexibleWidth: Boolean = false,
    flexibleHeight: Boolean = false,
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
    val withWidth = if (flexibleWidth) modifier else modifier.width(width)
    val sized = if (flexibleHeight) withWidth else withWidth.height(height)
    Box(
        modifier = sized.background(ImageProvider(bitmap)),
        contentAlignment = contentAlignment,
    ) {
        content()
    }
}
