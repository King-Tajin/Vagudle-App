package com.yellowskippergames.vagudle

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt

private val SIZE_COMPACT = DpSize(180.dp, 40.dp)
private val SIZE_EXPANDED = DpSize(180.dp, 110.dp)
private val COMPACT_ASPECT = SIZE_COMPACT.height.value / SIZE_COMPACT.width.value
private val EXPANDED_ASPECT = SIZE_EXPANDED.height.value / SIZE_EXPANDED.width.value
private val EXPANDED_ASPECT_THRESHOLD = (COMPACT_ASPECT + EXPANDED_ASPECT) / 2f
private const val DEEP_LINK_URL = "https://vagudle.king-tajin.dev/daily"

private fun fixedColor(color: Color) = ColorProvider(day = color, night = color)

private const val SLATE_ARGB = 0xFF45556C.toInt()
private const val NEAR_BLACK_ARGB = 0xFF0D0D1B.toInt()
private const val PLAQUE_FILL_ARGB = 0xFF0A0014.toInt()
private const val PLAQUE_BORDER_ARGB = 0xFF4E00A7.toInt()
private const val GOLD_ARGB = 0xFFFFD700.toInt()

private val GOLD = fixedColor(Color(0xFFFFD700))
private val NEAR_BLACK = fixedColor(Color(0xFF0D0D1B))
private val TEXT_WHITE = fixedColor(Color(0xFFFFFFFF))
private val MUTED_GRAY = fixedColor(Color(0xFF8A8A8A))
private val SOLVED_GREEN = fixedColor(Color(0xFF4A7C3F))
private val IN_PROGRESS_ORANGE = fixedColor(Color(0xFFFF8C00))
private val NOT_PLAYED_RED = fixedColor(Color(0xFFC41E3A))
private val NAVY = fixedColor(Color(0xFF0D1322))

private data class WidgetScale(
    val width: Float,
    val height: Float,
    val visual: Float,
    val textBoost: Float,
)

private const val COMPACT_TEXT_BOOST = 1.35f
private const val EXPANDED_TEXT_BOOST = 1f

private fun Dp.scaled(scale: Float): Dp = (this.value * scale).dp

private fun TextUnit.scaled(scale: WidgetScale): TextUnit = (this.value * scale.visual * scale.textBoost).sp

private fun Float.scaledSp(scale: WidgetScale): TextUnit = (this * scale.visual * scale.textBoost).sp

private fun dpToPx(
    dp: Dp,
    density: Float,
): Int = (dp.value * density).roundToInt().coerceAtLeast(1)

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

class DailyWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val data = loadDailyWidgetData(context)
        provideContent {
            DailyWidgetContent(context, data)
        }
    }
}

fun requestDailyWidgetUpdate(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        DailyWidget().updateAll(context)
    }
}

private fun openDailyIntent(context: Context): Intent =
    Intent(Intent.ACTION_VIEW, DEEP_LINK_URL.toUri()).apply {
        setPackage(context.packageName)
    }

@Composable
private fun DailyWidgetContent(
    context: Context,
    data: DailyWidgetData?,
) {
    val size = LocalSize.current
    val isExpanded = (size.height.value / size.width.value) >= EXPANDED_ASPECT_THRESHOLD
    val reference = if (isExpanded) SIZE_EXPANDED else SIZE_COMPACT
    val widthScale = size.width.value / reference.width.value
    val heightScale = size.height.value / reference.height.value
    val textBoost = if (isExpanded) EXPANDED_TEXT_BOOST else COMPACT_TEXT_BOOST
    val scale = WidgetScale(width = widthScale, height = heightScale, visual = sqrt(widthScale * heightScale), textBoost = textBoost)
    val borderThickness = 3.dp.scaled(scale.visual)
    val innerWidth = size.width - borderThickness * 2
    val innerHeight = size.height - borderThickness * 2
    val outerRadius = if (isExpanded) 15.dp else 16.dp
    val innerRadius = if (isExpanded) 12.dp else 13.dp

    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .cornerRadius(outerRadius.scaled(scale.visual))
                .background(GOLD)
                .clickable(actionStartActivity(openDailyIntent(context))),
    ) {
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = borderThickness, vertical = borderThickness)
                    .cornerRadius(innerRadius.scaled(scale.visual)),
        ) {
            if (data == null) {
                EmptyState(context, scale)
            } else {
                val isFresh = data.date == currentDailyDateUtc()
                if (isExpanded) {
                    ExpandedPanelContent(context, data, isFresh, scale, innerWidth, innerHeight)
                } else {
                    CompactPanelContent(context, data, isFresh, scale, innerWidth, innerHeight)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    context: Context,
    scale: WidgetScale,
) {
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(NEAR_BLACK),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = context.getString(R.string.widget_empty_state),
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 6.sp.scaled(scale), color = GOLD),
        )
    }
}

@Composable
private fun RoundedZoneBox(
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
private fun Plaque(
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
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = numberFontSize.scaled(scale), color = TEXT_WHITE),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CompactPanelContent(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
    innerWidth: Dp,
    innerHeight: Dp,
) {
    val density = context.resources.displayMetrics.density
    val leftZoneWidth = innerWidth * (50f / 174f)
    val gapWidth = innerWidth * (3f / 174f)
    val rightZoneWidth = innerWidth - leftZoneWidth - gapWidth
    Row(modifier = GlanceModifier.fillMaxSize()) {
        RoundedZoneBox(
            width = leftZoneWidth,
            height = innerHeight,
            fillColor = SLATE_ARGB,
            topLeftRadius = 13.dp.scaled(scale.visual),
            topRightRadius = 6.dp.scaled(scale.visual),
            bottomRightRadius = 6.dp.scaled(scale.visual),
            bottomLeftRadius = 13.dp.scaled(scale.visual),
            density = density,
        ) {
            Plaque(
                context = context,
                dailyNumber = data.dailyNumber,
                scale = scale,
                width = leftZoneWidth * (40f / 50f),
                height = innerHeight * (28f / 34f),
                labelFontSize = 4.sp,
                numberFontSize = 9.sp,
            )
        }
        Spacer(modifier = GlanceModifier.width(gapWidth).fillMaxHeight())
        RoundedZoneBox(
            width = rightZoneWidth,
            height = innerHeight,
            fillColor = NEAR_BLACK_ARGB,
            topLeftRadius = 6.dp.scaled(scale.visual),
            topRightRadius = 13.dp.scaled(scale.visual),
            bottomRightRadius = 13.dp.scaled(scale.visual),
            bottomLeftRadius = 6.dp.scaled(scale.visual),
            density = density,
        ) {
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp.scaled(scale.visual)),
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    StreakRow(context, data, scale)
                    Spacer(modifier = GlanceModifier.height(4.dp.scaled(scale.visual)))
                    StatusRow(context, data, isFresh, scale)
                }
            }
        }
    }
}

@Composable
private fun StreakRow(
    context: Context,
    data: DailyWidgetData,
    scale: WidgetScale,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_flame),
            contentDescription = null,
            modifier = GlanceModifier.width(2.92f.dp.scaled(scale.visual)).height(5.1f.dp.scaled(scale.visual)),
        )
        Spacer(modifier = GlanceModifier.width(2.dp.scaled(scale.visual)))
        Text(
            text = data.currentStreak.toString(),
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 7.sp.scaled(scale), color = GOLD),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            text = context.getString(R.string.widget_best_streak, data.bestStreak),
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 5.5f.sp.scaled(scale), color = MUTED_GRAY),
            maxLines = 1,
        )
    }
}

private data class StatusInfo(
    val text: String,
    val color: ColorProvider,
    val fontSize: TextUnit,
)

private fun statusInfo(
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
        return StatusInfo(context.getString(R.string.widget_in_progress), IN_PROGRESS_ORANGE, baseFontSize.scaled(scale))
    }
    return StatusInfo(context.getString(R.string.widget_not_played_yet), NOT_PLAYED_RED, notPlayedFontSize.scaled(scale))
}

private fun rankInfo(
    context: Context,
    rank: DailyWidgetRank,
    scale: WidgetScale,
    baseFontSize: TextUnit = 5.5f.sp,
    highRankFontSize: TextUnit = 4.5f.sp,
): Pair<String, TextUnit> =
    when (rank.status) {
        DailyWidgetRankStatus.RANKED -> {
            val position = rank.rank ?: 0
            val total = rank.outOf ?: 0
            if (total > 0 && position <= 8) {
                context.getString(R.string.widget_rank_top8) to baseFontSize.scaled(scale)
            } else {
                val fontSize = if (position >= 1000) highRankFontSize else baseFontSize
                context.getString(R.string.widget_rank_number, position) to fontSize.scaled(scale)
            }
        }
        DailyWidgetRankStatus.NO_USERNAME,
        DailyWidgetRankStatus.GUEST,
        -> "???" to baseFontSize.scaled(scale)
    }

@Composable
private fun StatusRow(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
) {
    val status = statusInfo(context, data, isFresh, scale)
    val (rankText, rankFontSize) = rankInfo(context, data.rank, scale)
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = status.text,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = status.fontSize, color = status.color),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Image(
            provider = ImageProvider(R.drawable.ic_widget_trophy),
            contentDescription = null,
            modifier = GlanceModifier.width(3.5f.dp.scaled(scale.visual)).height(4.05f.dp.scaled(scale.visual)),
        )
        Spacer(modifier = GlanceModifier.width(2.dp.scaled(scale.visual)))
        Text(
            text = rankText,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = rankFontSize, color = GOLD),
            maxLines = 1,
        )
    }
}

@Composable
private fun ExpandedPanelContent(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
    innerWidth: Dp,
    innerHeight: Dp,
) {
    val density = context.resources.displayMetrics.density
    val leftZoneWidth = innerWidth * (54f / 174f)
    val gapWidth = innerWidth * (3f / 174f)
    val rightZoneWidth = innerWidth - leftZoneWidth - gapWidth
    val sidePadding = 10.dp.scaled(scale.visual)
    Row(modifier = GlanceModifier.fillMaxSize()) {
        RoundedZoneBox(
            width = leftZoneWidth,
            height = innerHeight,
            fillColor = SLATE_ARGB,
            topLeftRadius = 12.dp.scaled(scale.visual),
            topRightRadius = 8.dp.scaled(scale.visual),
            bottomRightRadius = 8.dp.scaled(scale.visual),
            bottomLeftRadius = 12.dp.scaled(scale.visual),
            density = density,
        ) {
            Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                Plaque(
                    context = context,
                    dailyNumber = data.dailyNumber,
                    scale = scale,
                    width = leftZoneWidth * (38f / 54f),
                    height = innerHeight * (25f / 104f),
                    labelFontSize = 6.sp,
                    numberFontSize = 10.sp,
                )
                Spacer(modifier = GlanceModifier.height(6.dp.scaled(scale.visual)))
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_flame),
                        contentDescription = null,
                        modifier = GlanceModifier.width(6.29f.dp.scaled(scale.visual)).height(11.dp.scaled(scale.visual)),
                    )
                    Spacer(modifier = GlanceModifier.width(3.dp.scaled(scale.visual)))
                    Text(
                        text = data.currentStreak.toString(),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp.scaled(scale), color = GOLD),
                        maxLines = 1,
                    )
                }
                Text(
                    text = context.getString(R.string.widget_streak_label),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 6.sp.scaled(scale), color = GOLD),
                    maxLines = 1,
                )
                Spacer(modifier = GlanceModifier.height(6.dp.scaled(scale.visual)))
                Text(
                    text = context.getString(R.string.widget_best_streak, data.bestStreak),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 8.sp.scaled(scale), color = MUTED_GRAY),
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = GlanceModifier.width(gapWidth).fillMaxHeight())
        RoundedZoneBox(
            width = rightZoneWidth,
            height = innerHeight,
            fillColor = NEAR_BLACK_ARGB,
            topLeftRadius = 8.dp.scaled(scale.visual),
            topRightRadius = 12.dp.scaled(scale.visual),
            bottomRightRadius = 12.dp.scaled(scale.visual),
            bottomLeftRadius = 8.dp.scaled(scale.visual),
            density = density,
        ) {
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .padding(horizontal = sidePadding, vertical = 8.dp.scaled(scale.visual)),
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    ExpandedStatusLine(context, data, isFresh, scale)
                    Spacer(modifier = GlanceModifier.height(6.dp.scaled(scale.visual)))
                    ExpandedRankRow(context, data, scale)
                    Spacer(modifier = GlanceModifier.height(4.dp.scaled(scale.visual)))
                    Text(
                        text =
                            context.getString(
                                R.string.widget_daily_info_short,
                                data.wordLength,
                                context.getString(if (data.hardMode) R.string.widget_mode_hard else R.string.widget_mode_normal),
                            ),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 8.sp.scaled(scale), color = MUTED_GRAY),
                        maxLines = 1,
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp.scaled(scale.visual)))
                    PlayButton(
                        context = context,
                        data = data,
                        isFresh = isFresh,
                        scale = scale,
                        buttonWidth = rightZoneWidth - sidePadding * 2,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedStatusLine(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
) {
    val status = statusInfo(context, data, isFresh, scale, baseFontSize = 12.sp, uniformSize = true)
    Text(
        text = status.text,
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = status.fontSize, color = status.color),
        maxLines = 1,
    )
}

@Composable
private fun ExpandedRankRow(
    context: Context,
    data: DailyWidgetData,
    scale: WidgetScale,
) {
    val (rankText, rankFontSize) = rankInfo(context, data.rank, scale, baseFontSize = 10.sp, highRankFontSize = 8.sp)
    Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_trophy),
            contentDescription = null,
            modifier = GlanceModifier.width(9.5f.dp.scaled(scale.visual)).height(11.dp.scaled(scale.visual)),
        )
        Spacer(modifier = GlanceModifier.width(4.dp.scaled(scale.visual)))
        Text(
            text = rankText,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = rankFontSize, color = GOLD),
            maxLines = 1,
        )
    }
}

private fun playButtonLabel(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
): String =
    when {
        isFresh && data.hasPlayedToday -> context.getString(R.string.widget_play_normal_mode)
        isFresh && data.inProgress -> context.getString(R.string.widget_resume)
        else -> context.getString(R.string.widget_play_daily)
    }

private fun playButtonFontSize(
    labelLength: Int,
    buttonWidth: Dp,
    scale: WidgetScale,
): TextUnit {
    val iconAndGapOverheadDp = 24f
    val pillPaddingDp = 16f
    val referenceButtonWidthDp = buttonWidth.value / scale.width
    val availableTextWidthDp = referenceButtonWidthDp - iconAndGapOverheadDp - pillPaddingDp
    val rawSize = availableTextWidthDp / (labelLength * 0.56f)
    return rawSize.coerceIn(6f, 9f).scaledSp(scale)
}

@Composable
private fun PlayButton(
    context: Context,
    data: DailyWidgetData,
    isFresh: Boolean,
    scale: WidgetScale,
    buttonWidth: Dp,
) {
    val density = context.resources.displayMetrics.density
    val buttonHeight = 16.dp.scaled(scale.visual)
    val label = playButtonLabel(context, data, isFresh)
    val fontSize = playButtonFontSize(label.length, buttonWidth, scale)
    val cornerRadius = buttonHeight / 2
    RoundedZoneBox(
        width = buttonWidth,
        height = buttonHeight,
        fillColor = GOLD_ARGB,
        topLeftRadius = cornerRadius,
        topRightRadius = cornerRadius,
        bottomRightRadius = cornerRadius,
        bottomLeftRadius = cornerRadius,
        density = density,
        modifier = GlanceModifier.clickable(actionStartActivity(openDailyIntent(context))),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_play),
                contentDescription = null,
                modifier = GlanceModifier.width(6.dp.scaled(scale.visual)).height(7.5f.dp.scaled(scale.visual)),
            )
            Spacer(modifier = GlanceModifier.width(4.dp.scaled(scale.visual)))
            Text(
                text = label,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize, color = NAVY),
                maxLines = 1,
            )
        }
    }
}
