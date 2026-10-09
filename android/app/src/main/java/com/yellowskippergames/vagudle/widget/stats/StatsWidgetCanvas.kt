package com.yellowskippergames.vagudle.widget.stats

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.yellowskippergames.vagudle.R
import com.yellowskippergames.vagudle.widget.core.widgetMessage
import com.yellowskippergames.vagudle.widget.ui.GOLD_ARGB
import com.yellowskippergames.vagudle.widget.ui.MUTED_GRAY_ARGB
import com.yellowskippergames.vagudle.widget.ui.NEAR_BLACK_ARGB
import com.yellowskippergames.vagudle.widget.ui.NOT_PLAYED_RED_ARGB
import com.yellowskippergames.vagudle.widget.ui.PLAQUE_BORDER_ARGB
import com.yellowskippergames.vagudle.widget.ui.SLATE_ARGB
import com.yellowskippergames.vagudle.widget.ui.SOLVED_GREEN_ARGB
import com.yellowskippergames.vagudle.widget.ui.TEXT_WHITE_ARGB
import com.yellowskippergames.vagudle.widget.ui.WidgetFrame
import com.yellowskippergames.vagudle.widget.ui.WidgetSplitLayout
import com.yellowskippergames.vagudle.widget.ui.dpToPx
import java.text.NumberFormat
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal const val STATS_LEFT_BOX_WIDTH = 88f
internal const val STATS_RIGHT_BOX_WIDTH = 153f
internal const val STATS_BOX_HEIGHT = 104f
private const val MAX_EXTRA_HEIGHT = STATS_BOX_HEIGHT
private const val MAX_STATS_BITMAP_PIXELS = 600_000f
private const val LEFT_ORIGIN_X = 3f
private const val RIGHT_ORIGIN_X = 94f
private const val ORIGIN_Y = 3f
private const val MEASURE_SIZE = 100f
private const val STATS_RING_TRACK_ARGB = 0xFF2F3B4E.toInt()
private const val STATS_DIVIDER_ARGB = 0xFF1F2937.toInt()
private const val STATS_LABEL_ARGB = 0xFFCBD5E1.toInt()

private const val RING_CENTER_X = 47f
private const val RING_CENTER_Y = 40f
private const val OUTER_RING_RADIUS = 29f
private const val INNER_RING_RADIUS = 21f
private const val RING_STROKE = 6f
private const val DISC_RADIUS = OUTER_RING_RADIUS + RING_STROKE / 2f
private const val LEGEND_PLATE_LEFT = 8f
private const val LEGEND_PLATE_TOP = 77f
private const val LEGEND_PLATE_RIGHT = 86f
private const val LEGEND_PLATE_BOTTOM = 102.5f
private const val LEGEND_PLATE_RADIUS = 6f
private const val GAMES_BASELINE = 41.5f
private const val GAMES_LABEL_BASELINE = 49f
private const val LEGEND_DOT_RADIUS = 2.5f
private const val LEGEND_DOT_Y = 85.5f
private const val LEGEND_VALUE_BASELINE = 89f
private const val LEGEND_LABEL_BASELINE = 98f
private const val NORMAL_LEGEND_DOT_X = 16.5f
private const val NORMAL_LEGEND_TEXT_X = 22f
private const val HARD_LEGEND_DOT_X = 52.5f
private const val HARD_LEGEND_TEXT_X = 58f
private const val LEGEND_LABEL_BUDGET = 27f

private const val NORMAL_COLUMN_X = 102f
private const val HARD_COLUMN_X = 175f
private const val COLUMN_WIDTH = 64f
private const val COLUMN_DIVIDER_X = 170.5f
private const val COLUMN_DIVIDER_TOP = 13f
private const val COLUMN_DIVIDER_BOTTOM = 98f
private const val HEADER_DOT_Y = 15f
private const val HEADER_BASELINE = 17.5f
private const val BAR_WIDTH = 8f
private const val BAR_PITCH = 11f
private const val BAR_BOTTOM = 56f
private const val BAR_MAX_HEIGHT = 28f
private const val BAR_MIN_HEIGHT = 2f
private const val BAR_LABEL_BASELINE = 64f
private const val CAPTION_BASELINE = 72f
private const val CAPTION_DIVIDER_Y = 77f
private const val FLAME_TOP = 83f
private const val FLAME_WIDTH = 8.5f
private const val FLAME_HEIGHT = 10.4f
private const val TROPHY_OFFSET_X = 35f
private const val TROPHY_TOP = 83.5f
private const val TROPHY_SIZE = 9.4f
private const val STREAK_BASELINE = 93f
private const val CURRENT_STREAK_OFFSET_X = 12f
private const val BEST_STREAK_OFFSET_X = 47f
private const val STREAKS_BASELINE = 102f
private const val CAPTION_MIN_SIZE = 3.5f
private const val NUMBER_MIN_SIZE = 7f

private const val MESSAGE_CENTER_X = 170.5f
private const val MESSAGE_TITLE_BASELINE = 50f
private const val MESSAGE_SUBTITLE_BASELINE = 62f
private const val MESSAGE_BUDGET = 140f

private class StatsPainter(
    private val canvas: Canvas,
    private val unit: Float,
    private val originX: Float,
    private val originY: Float,
    private val designX: Float,
    private val font: Typeface?,
    val extraHeight: Float,
) {
    private var offsetY = 0f

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = font }
    private val measurePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = font
            textSize = MEASURE_SIZE
        }

    private fun px(x: Float) = originX + (x - designX) * unit

    private fun py(y: Float) = originY + (y + offsetY - ORIGIN_Y) * unit

    fun shifted(
        amount: Float,
        block: () -> Unit,
    ) {
        val previous = offsetY
        offsetY = previous + amount
        block()
        offsetY = previous
    }

    private fun len(value: Float) = value * unit

    fun dot(
        cx: Float,
        cy: Float,
        radius: Float,
        color: Int,
    ) {
        fillPaint.color = color
        canvas.drawCircle(px(cx), py(cy), len(radius), fillPaint)
    }

    fun plate(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Float,
        color: Int,
    ) {
        fillPaint.color = color
        canvas.drawRoundRect(RectF(px(left), py(top), px(right), py(bottom)), len(radius), len(radius), fillPaint)
    }

    fun ring(
        cx: Float,
        cy: Float,
        radius: Float,
        fraction: Float,
        color: Int,
    ) {
        val bounds = RectF(px(cx) - len(radius), py(cy) - len(radius), px(cx) + len(radius), py(cy) + len(radius))
        strokePaint.strokeWidth = len(RING_STROKE)
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.color = STATS_RING_TRACK_ARGB
        canvas.drawOval(bounds, strokePaint)
        if (fraction > 0f) {
            strokePaint.color = color
            canvas.drawArc(bounds, -90f, 360f * fraction.coerceAtMost(1f), false, strokePaint)
        }
    }

    fun bar(
        left: Float,
        height: Float,
        color: Int,
    ) {
        val radius = if (height <= 4.2f) 1f else 1.5f
        fillPaint.color = color
        canvas.drawRoundRect(
            RectF(px(left), py(BAR_BOTTOM - height), px(left + BAR_WIDTH), py(BAR_BOTTOM)),
            len(radius),
            len(radius),
            fillPaint,
        )
    }

    fun line(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: Int,
    ) {
        strokePaint.strokeWidth = len(1f)
        strokePaint.strokeCap = Paint.Cap.BUTT
        strokePaint.color = color
        canvas.drawLine(px(x1), py(y1), px(x2), py(y2), strokePaint)
    }

    fun icon(
        drawable: Drawable?,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
    ) {
        drawable ?: return
        drawable.setBounds(
            px(left).roundToInt(),
            py(top).roundToInt(),
            px(left + width).roundToInt(),
            py(top + height).roundToInt(),
        )
        drawable.draw(canvas)
    }

    fun text(
        value: String,
        x: Float,
        baseline: Float,
        size: Float,
        color: Int,
        align: Paint.Align = Paint.Align.LEFT,
        spacing: Float = 0f,
    ) {
        textPaint.textSize = len(size)
        textPaint.letterSpacing = spacing
        textPaint.textAlign = align
        textPaint.color = color
        canvas.drawText(value, px(x), py(baseline), textPaint)
    }

    fun fitSize(
        value: String,
        maxSize: Float,
        budget: Float,
        minSize: Float,
        spacing: Float = 0f,
    ): Float {
        measurePaint.letterSpacing = spacing
        val width = measurePaint.measureText(value) * maxSize / MEASURE_SIZE
        if (width <= budget) return maxSize
        return max(minSize, maxSize * budget / width)
    }

    fun fittedText(
        value: String,
        x: Float,
        baseline: Float,
        maxSize: Float,
        budget: Float,
        minSize: Float,
        color: Int,
        align: Paint.Align = Paint.Align.LEFT,
        spacing: Float = 0f,
    ) {
        text(value, x, baseline, fitSize(value, maxSize, budget, minSize, spacing), color, align, spacing)
    }
}

private fun fillShape(
    canvas: Canvas,
    paint: Paint,
    bounds: RectF,
    color: Int,
    topLeft: Float,
    topRight: Float,
    bottomRight: Float,
    bottomLeft: Float,
) {
    val radii =
        floatArrayOf(
            topLeft,
            topLeft,
            topRight,
            topRight,
            bottomRight,
            bottomRight,
            bottomLeft,
            bottomLeft,
        )
    paint.color = color
    canvas.drawPath(Path().apply { addRoundRect(bounds, radii, Path.Direction.CW) }, paint)
}

private fun zonePainter(
    canvas: Canvas,
    font: Typeface?,
    bounds: RectF,
    boxWidth: Float,
    designX: Float,
    visualUnit: Float,
): StatsPainter {
    val unit = minOf(visualUnit, bounds.width() / boxWidth, bounds.height() / STATS_BOX_HEIGHT)
    val extra = (bounds.height() / unit - STATS_BOX_HEIGHT).coerceIn(0f, MAX_EXTRA_HEIGHT)
    return StatsPainter(
        canvas = canvas,
        unit = unit,
        originX = bounds.left + (bounds.width() - boxWidth * unit) / 2f,
        originY = bounds.top + (bounds.height() - (STATS_BOX_HEIGHT + extra) * unit) / 2f,
        designX = designX,
        font = font,
        extraHeight = extra,
    )
}

internal fun statsWidgetBitmap(
    context: Context,
    viewState: StatsWidgetViewState,
    frame: WidgetFrame,
    split: WidgetSplitLayout,
    visualScale: Float,
): Bitmap {
    val rawPixels = frame.size.width.value * frame.density * frame.size.height.value * frame.density
    val renderDensity =
        if (rawPixels > MAX_STATS_BITMAP_PIXELS) {
            frame.density * sqrt(MAX_STATS_BITMAP_PIXELS / rawPixels)
        } else {
            frame.density
        }
    val widthPx = dpToPx(frame.size.width, renderDensity)
    val heightPx = dpToPx(frame.size.height, renderDensity)
    val bitmap = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bitmap)
    val font = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_bold)
    val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    val border = frame.borderThickness.value * renderDensity
    val gap = split.gap.value * renderDensity
    val leftWidth = split.leftWidth.value * renderDensity
    val outer = frame.outerRadius.value * renderDensity
    val inner = frame.innerRadius.value * renderDensity
    val inward = split.inwardRadius.value * renderDensity
    val zoneBottom = heightPx - border
    val leftBounds = RectF(border, border, border + leftWidth, zoneBottom)
    val rightBounds = RectF(leftBounds.right + gap, border, widthPx - border, zoneBottom)

    fillShape(
        canvas,
        shapePaint,
        RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat()),
        GOLD_ARGB,
        outer,
        outer,
        outer,
        outer,
    )
    fillShape(canvas, shapePaint, leftBounds, SLATE_ARGB, inner, inward, inward, inner)
    fillShape(canvas, shapePaint, rightBounds, NEAR_BLACK_ARGB, inward, inner, inner, inward)

    val visualUnit = visualScale * renderDensity
    zonePainter(canvas, font, leftBounds, STATS_LEFT_BOX_WIDTH, LEFT_ORIGIN_X, visualUnit)
        .drawLeft(context, viewState.data)
    zonePainter(canvas, font, rightBounds, STATS_RIGHT_BOX_WIDTH, RIGHT_ORIGIN_X, visualUnit)
        .drawRight(context, viewState)
    return bitmap
}

private fun spacingEm(
    spacing: Float,
    size: Float,
) = spacing / size

private fun StatsPainter.drawLeft(
    context: Context,
    data: StatsWidgetData?,
) {
    val shown = data ?: emptyStatsWidgetData()
    dot(RING_CENTER_X, RING_CENTER_Y, DISC_RADIUS, NEAR_BLACK_ARGB)
    ring(RING_CENTER_X, RING_CENTER_Y, OUTER_RING_RADIUS, shown.normal.successRate / 100f, GOLD_ARGB)
    ring(RING_CENTER_X, RING_CENTER_Y, INNER_RING_RADIUS, shown.hard.successRate / 100f, PLAQUE_BORDER_ARGB)
    fittedText(
        value = NumberFormat.getIntegerInstance().format(shown.totalGames),
        x = RING_CENTER_X,
        baseline = GAMES_BASELINE,
        maxSize = 12f,
        budget = 30f,
        minSize = NUMBER_MIN_SIZE,
        color = TEXT_WHITE_ARGB,
        align = Paint.Align.CENTER,
    )
    fittedText(
        value = context.getString(R.string.widget_stats_games),
        x = RING_CENTER_X,
        baseline = GAMES_LABEL_BASELINE,
        maxSize = 5.5f,
        budget = 30f,
        minSize = CAPTION_MIN_SIZE,
        color = STATS_LABEL_ARGB,
        align = Paint.Align.CENTER,
        spacing = spacingEm(0.5f, 5.5f),
    )
    shifted(extraHeight) {
        plate(
            LEGEND_PLATE_LEFT,
            LEGEND_PLATE_TOP,
            LEGEND_PLATE_RIGHT,
            LEGEND_PLATE_BOTTOM,
            LEGEND_PLATE_RADIUS,
            NEAR_BLACK_ARGB,
        )
        legend(
            context,
            NORMAL_LEGEND_DOT_X,
            NORMAL_LEGEND_TEXT_X,
            GOLD_ARGB,
            shown.normal.successRate,
            R.string.widget_mode_normal,
        )
        legend(
            context,
            HARD_LEGEND_DOT_X,
            HARD_LEGEND_TEXT_X,
            PLAQUE_BORDER_ARGB,
            shown.hard.successRate,
            R.string.widget_mode_hard,
        )
    }
}

private fun StatsPainter.legend(
    context: Context,
    dotX: Float,
    textX: Float,
    color: Int,
    successRate: Int,
    labelRes: Int,
) {
    dot(dotX, LEGEND_DOT_Y, LEGEND_DOT_RADIUS, color)
    fittedText(
        value = "$successRate%",
        x = textX,
        baseline = LEGEND_VALUE_BASELINE,
        maxSize = 12f,
        budget = 26f,
        minSize = NUMBER_MIN_SIZE,
        color = color,
    )
    fittedText(
        value = context.getString(labelRes).uppercase(),
        x = textX,
        baseline = LEGEND_LABEL_BASELINE,
        maxSize = 5f,
        budget = LEGEND_LABEL_BUDGET,
        minSize = CAPTION_MIN_SIZE,
        color = STATS_LABEL_ARGB,
        spacing = spacingEm(0.5f, 5f),
    )
}

private fun StatsPainter.drawRight(
    context: Context,
    viewState: StatsWidgetViewState,
) {
    val data = viewState.data
    if (data == null) {
        shifted(extraHeight / 2f) { message(context, viewState.setupFailed) }
    } else {
        line(
            COLUMN_DIVIDER_X,
            COLUMN_DIVIDER_TOP,
            COLUMN_DIVIDER_X,
            COLUMN_DIVIDER_BOTTOM + extraHeight,
            STATS_DIVIDER_ARGB,
        )
        modeColumn(context, data.normal, NORMAL_COLUMN_X, GOLD_ARGB, R.string.widget_mode_normal)
        modeColumn(context, data.hard, HARD_COLUMN_X, PLAQUE_BORDER_ARGB, R.string.widget_mode_hard)
    }
}

private fun StatsPainter.message(
    context: Context,
    setupFailed: Boolean,
) {
    val message = widgetMessage(context, setupFailed)
    fittedText(
        value = message.title,
        x = MESSAGE_CENTER_X,
        baseline = MESSAGE_TITLE_BASELINE,
        maxSize = 8f,
        budget = MESSAGE_BUDGET,
        minSize = 5f,
        color = if (message.isError) NOT_PLAYED_RED_ARGB else GOLD_ARGB,
        align = Paint.Align.CENTER,
    )
    fittedText(
        value = message.subtitle,
        x = MESSAGE_CENTER_X,
        baseline = MESSAGE_SUBTITLE_BASELINE,
        maxSize = 6f,
        budget = MESSAGE_BUDGET,
        minSize = 4f,
        color = MUTED_GRAY_ARGB,
        align = Paint.Align.CENTER,
    )
}

private fun bucketLabel(
    mode: StatsModeData,
    index: Int,
): String = if (index == 0) "\u2264${mode.firstBucketMax}" else (mode.firstBucketMax + index).toString()

private fun StatsPainter.modeColumn(
    context: Context,
    mode: StatsModeData,
    x0: Float,
    accent: Int,
    labelRes: Int,
) {
    val center = x0 + COLUMN_WIDTH / 2f
    dot(x0 + 2f, HEADER_DOT_Y, 2f, accent)
    fittedText(
        value = context.getString(labelRes).uppercase(),
        x = x0 + 7f,
        baseline = HEADER_BASELINE,
        maxSize = 6.5f,
        budget = COLUMN_WIDTH - 7f,
        minSize = CAPTION_MIN_SIZE,
        color = MUTED_GRAY_ARGB,
        spacing = spacingEm(0.6f, 6.5f),
    )

    shifted(extraHeight) {
        val highest = max(mode.distribution.maxOrNull() ?: 0, 0)
        val topBucket = mode.mostCommonBucket
        mode.distribution.forEachIndexed { index, count ->
            val height =
                if (highest > 0) {
                    max(BAR_MIN_HEIGHT, (BAR_MAX_HEIGHT + extraHeight) * count / highest)
                } else {
                    BAR_MIN_HEIGHT
                }
            val left = x0 + index * BAR_PITCH
            bar(left, height, if (index == topBucket) SOLVED_GREEN_ARGB else SLATE_ARGB)
            text(
                value = bucketLabel(mode, index),
                x = left + BAR_WIDTH / 2f,
                baseline = BAR_LABEL_BASELINE,
                size = 6f,
                color = MUTED_GRAY_ARGB,
                align = Paint.Align.CENTER,
            )
        }

        fittedText(
            value = context.getString(R.string.widget_stats_guess_distribution),
            x = center,
            baseline = CAPTION_BASELINE,
            maxSize = 5f,
            budget = COLUMN_WIDTH,
            minSize = CAPTION_MIN_SIZE,
            color = MUTED_GRAY_ARGB,
            align = Paint.Align.CENTER,
            spacing = spacingEm(0.2f, 5f),
        )
        line(x0, CAPTION_DIVIDER_Y, x0 + COLUMN_WIDTH, CAPTION_DIVIDER_Y, STATS_RING_TRACK_ARGB)

        icon(ContextCompat.getDrawable(context, R.drawable.ic_widget_flame), x0, FLAME_TOP, FLAME_WIDTH, FLAME_HEIGHT)
        fittedText(
            value = mode.currentStreak.toString(),
            x = x0 + CURRENT_STREAK_OFFSET_X,
            baseline = STREAK_BASELINE,
            maxSize = 10f,
            budget = 20f,
            minSize = NUMBER_MIN_SIZE,
            color = TEXT_WHITE_ARGB,
        )
        icon(
            ContextCompat.getDrawable(context, R.drawable.ic_widget_trophy),
            x0 + TROPHY_OFFSET_X,
            TROPHY_TOP,
            TROPHY_SIZE,
            TROPHY_SIZE,
        )
        fittedText(
            value = mode.bestStreak.toString(),
            x = x0 + BEST_STREAK_OFFSET_X,
            baseline = STREAK_BASELINE,
            maxSize = 10f,
            budget = 17f,
            minSize = NUMBER_MIN_SIZE,
            color = TEXT_WHITE_ARGB,
        )
        fittedText(
            value = context.getString(R.string.widget_stats_streaks),
            x = center,
            baseline = STREAKS_BASELINE,
            maxSize = 5.5f,
            budget = COLUMN_WIDTH,
            minSize = CAPTION_MIN_SIZE,
            color = MUTED_GRAY_ARGB,
            align = Paint.Align.CENTER,
            spacing = spacingEm(0.6f, 5.5f),
        )
    }
}
