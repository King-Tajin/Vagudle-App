package com.yellowskippergames.vagudle.widget.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import kotlin.math.max

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

private enum class Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT }

private fun cornerPieceBitmap(
    widthPx: Int,
    heightPx: Int,
    fillColor: Int,
    corner: Corner,
    radiusPx: Float,
): Bitmap =
    roundedRectBitmap(
        widthPx = widthPx,
        heightPx = heightPx,
        fillColor = fillColor,
        topLeftRadius = if (corner == Corner.TOP_LEFT) radiusPx else 0f,
        topRightRadius = if (corner == Corner.TOP_RIGHT) radiusPx else 0f,
        bottomRightRadius = if (corner == Corner.BOTTOM_RIGHT) radiusPx else 0f,
        bottomLeftRadius = if (corner == Corner.BOTTOM_LEFT) radiusPx else 0f,
    )

private fun radiusToPx(
    radiusDp: Float,
    density: Float,
): Int = if (radiusDp <= 0f) 0 else dpToPx(radiusDp.dp, density)

private fun pxToDp(
    px: Int,
    density: Float,
): Dp = (px / density).dp

@Composable
private fun SliceRow(
    heightPx: Int,
    leftWidthPx: Int,
    rightWidthPx: Int,
    leftBitmap: Bitmap?,
    rightBitmap: Bitmap?,
    fillColor: Int,
    density: Float,
) {
    val rowHeight = pxToDp(heightPx, density)
    Row(modifier = GlanceModifier.fillMaxWidth().height(rowHeight)) {
        if (leftBitmap != null) {
            Image(
                provider = ImageProvider(leftBitmap),
                contentDescription = null,
                modifier = GlanceModifier.width(pxToDp(leftWidthPx, density)).height(rowHeight),
                contentScale = ContentScale.FillBounds,
            )
        }
        Box(
            modifier = GlanceModifier.defaultWeight().fillMaxHeight().background(Color(fillColor)),
        ) {}
        if (rightBitmap != null) {
            Image(
                provider = ImageProvider(rightBitmap),
                contentDescription = null,
                modifier = GlanceModifier.width(pxToDp(rightWidthPx, density)).height(rowHeight),
                contentScale = ContentScale.FillBounds,
            )
        }
    }
}

@Composable
private fun SlicedZoneBox(
    fillColor: Int,
    topLeft: Float,
    topRight: Float,
    bottomRight: Float,
    bottomLeft: Float,
    density: Float,
    contentAlignment: Alignment,
    modifier: GlanceModifier,
    content: @Composable () -> Unit,
) {
    val topLeftPx = radiusToPx(topLeft, density)
    val topRightPx = radiusToPx(topRight, density)
    val bottomRightPx = radiusToPx(bottomRight, density)
    val bottomLeftPx = radiusToPx(bottomLeft, density)
    val leftWidthPx = max(topLeftPx, bottomLeftPx)
    val rightWidthPx = max(topRightPx, bottomRightPx)
    val topHeightPx = max(topLeftPx, topRightPx)
    val bottomHeightPx = max(bottomLeftPx, bottomRightPx)

    Box(modifier = modifier, contentAlignment = contentAlignment) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            if (topHeightPx > 0) {
                SliceRow(
                    heightPx = topHeightPx,
                    leftWidthPx = leftWidthPx,
                    rightWidthPx = rightWidthPx,
                    leftBitmap =
                        if (leftWidthPx > 0) {
                            cornerPieceBitmap(leftWidthPx, topHeightPx, fillColor, Corner.TOP_LEFT, topLeftPx.toFloat())
                        } else {
                            null
                        },
                    rightBitmap =
                        if (rightWidthPx > 0) {
                            cornerPieceBitmap(
                                rightWidthPx,
                                topHeightPx,
                                fillColor,
                                Corner.TOP_RIGHT,
                                topRightPx.toFloat(),
                            )
                        } else {
                            null
                        },
                    fillColor = fillColor,
                    density = density,
                )
            }
            Box(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight().background(Color(fillColor)),
            ) {}
            if (bottomHeightPx > 0) {
                SliceRow(
                    heightPx = bottomHeightPx,
                    leftWidthPx = leftWidthPx,
                    rightWidthPx = rightWidthPx,
                    leftBitmap =
                        if (leftWidthPx > 0) {
                            cornerPieceBitmap(
                                leftWidthPx,
                                bottomHeightPx,
                                fillColor,
                                Corner.BOTTOM_LEFT,
                                bottomLeftPx.toFloat(),
                            )
                        } else {
                            null
                        },
                    rightBitmap =
                        if (rightWidthPx > 0) {
                            cornerPieceBitmap(
                                rightWidthPx,
                                bottomHeightPx,
                                fillColor,
                                Corner.BOTTOM_RIGHT,
                                bottomRightPx.toFloat(),
                            )
                        } else {
                            null
                        },
                    fillColor = fillColor,
                    density = density,
                )
            }
        }
        content()
    }
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
    val isFlexible = flexibleWidth || flexibleHeight
    if (isFlexible && strokeWidth.value <= 0f) {
        val maxRadius = minOf(width.value, height.value) / 2f
        SlicedZoneBox(
            fillColor = fillColor,
            topLeft = topLeftRadius.value.coerceIn(0f, maxRadius),
            topRight = topRightRadius.value.coerceIn(0f, maxRadius),
            bottomRight = bottomRightRadius.value.coerceIn(0f, maxRadius),
            bottomLeft = bottomLeftRadius.value.coerceIn(0f, maxRadius),
            density = density,
            contentAlignment = contentAlignment,
            modifier =
                modifier
                    .let {
                        if (flexibleWidth) it else it.width(width)
                    }.let { if (flexibleHeight) it else it.height(height) },
            content = content,
        )
        return
    }
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
