package com.yellowskippergames.vagudle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.height
import androidx.glance.layout.width
import kotlin.math.roundToInt

internal const val COIN_BOX_DP = 34f
private const val COIN_CENTER_X = 17f
private const val COIN_CENTER_Y = 15.5f
private const val COIN_DISK_RADIUS = 14f
private const val GAUGE_RADIUS = 12.5f
private const val GAUGE_STROKE = 2.6f
private const val GAUGE_START_ANGLE = 135f
private const val GAUGE_SWEEP = 270f
private const val RIBBON_WIDTH = 17.32f
private const val RIBBON_HEIGHT = 19f
private const val RIBBON_TOP = 6.3f
private const val PILL_TOP = 25.4f
private const val PILL_HEIGHT = 7.6f
private const val PILL_MIN_WIDTH = 16f
private const val PILL_MAX_WIDTH = 30f
private const val PILL_CHAR_WIDTH = 3.64f
private const val PILL_PADDING = 5f
private const val COUNT_BASELINE = 31.2f
private const val COUNT_TEXT_SIZE = 6.5f
private const val FAILED_TRACK_ARGB = 0xFFC41E3A.toInt()
private const val WHITE_ARGB = 0xFFFFFFFF.toInt()

internal data class CoinModel(
    val countText: String,
    val progressFraction: Float,
    val trackColor: Int,
    val contentDescription: String,
)

internal fun coinModel(
    context: Context,
    viewState: AchievementsWidgetViewState,
): CoinModel {
    val data = viewState.data
    val label = context.getString(R.string.achievements_widget_label)
    return when (data) {
        null if viewState.setupFailed ->
            CoinModel(context.getString(R.string.widget_achievements_retry), 0f, FAILED_TRACK_ARGB, label)
        null ->
            CoinModel(context.getString(R.string.widget_achievements_tap), 0f, PLAQUE_BORDER_ARGB, label)
        else ->
            CoinModel(
                countText =
                    context.getString(
                        R.string.widget_achievements_fraction,
                        data.unlockedCount,
                        data.totalAchievements,
                    ),
                progressFraction = data.unlockedCount.toFloat() / data.totalAchievements,
                trackColor = PLAQUE_BORDER_ARGB,
                contentDescription =
                    context.getString(
                        R.string.widget_achievements_content_description,
                        data.unlockedCount,
                        data.totalAchievements,
                    ),
            )
    }
}

@Composable
internal fun AchievementsCoin(
    context: Context,
    model: CoinModel,
    scale: Float,
    density: Float,
) {
    val sizeDp = (COIN_BOX_DP * scale).dp
    val bitmap = achievementsCoinBitmap(context, model, dpToPx(sizeDp, density))
    Image(
        provider = ImageProvider(bitmap),
        contentDescription = model.contentDescription,
        modifier = GlanceModifier.width(sizeDp).height(sizeDp),
    )
}

private fun achievementsCoinBitmap(
    context: Context,
    model: CoinModel,
    sizePx: Int,
): Bitmap {
    val unit = sizePx / COIN_BOX_DP
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    val centerX = COIN_CENTER_X * unit
    val centerY = COIN_CENTER_Y * unit

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PLAQUE_FILL_ARGB }
    canvas.drawCircle(centerX, centerY, COIN_DISK_RADIUS * unit, fillPaint)

    val gaugeBounds =
        RectF(
            centerX - GAUGE_RADIUS * unit,
            centerY - GAUGE_RADIUS * unit,
            centerX + GAUGE_RADIUS * unit,
            centerY + GAUGE_RADIUS * unit,
        )
    val gaugePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = GAUGE_STROKE * unit
            strokeCap = Paint.Cap.ROUND
        }
    gaugePaint.color = model.trackColor
    canvas.drawArc(gaugeBounds, GAUGE_START_ANGLE, GAUGE_SWEEP, false, gaugePaint)
    if (model.progressFraction > 0f) {
        gaugePaint.color = GOLD_ARGB
        canvas.drawArc(gaugeBounds, GAUGE_START_ANGLE, GAUGE_SWEEP * model.progressFraction, false, gaugePaint)
    }

    ContextCompat.getDrawable(context, R.drawable.ic_widget_ribbon)?.apply {
        val left = (centerX - RIBBON_WIDTH / 2f * unit).roundToInt()
        val top = (RIBBON_TOP * unit).roundToInt()
        setBounds(left, top, left + (RIBBON_WIDTH * unit).roundToInt(), top + (RIBBON_HEIGHT * unit).roundToInt())
        draw(canvas)
    }

    val pillWidth = (model.countText.length * PILL_CHAR_WIDTH + PILL_PADDING).coerceIn(PILL_MIN_WIDTH, PILL_MAX_WIDTH)
    val pillBounds =
        RectF(
            centerX - pillWidth / 2f * unit,
            PILL_TOP * unit,
            centerX + pillWidth / 2f * unit,
            (PILL_TOP + PILL_HEIGHT) * unit,
        )
    canvas.drawRoundRect(pillBounds, PILL_HEIGHT / 2f * unit, PILL_HEIGHT / 2f * unit, fillPaint)

    val textPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE_ARGB
            textAlign = Paint.Align.CENTER
            textSize = COUNT_TEXT_SIZE * unit
            typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_bold)
        }
    canvas.drawText(model.countText, centerX, COUNT_BASELINE * unit, textPaint)
    return bitmap
}
