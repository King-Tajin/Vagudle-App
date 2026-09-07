package com.yellowskippergames.vagudle

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap

private val REFERENCE_WIDTH = 180.dp
private val REFERENCE_HEIGHT = 40.dp
private const val DEEP_LINK_URL = "https://vagudle.king-tajin.dev/daily"

private fun fixedColor(color: Color) = ColorProvider(day = color, night = color)

private const val SLATE_ARGB = 0xFF45556C.toInt()
private const val NEAR_BLACK_ARGB = 0xFF0D0D1B.toInt()
private const val PLAQUE_FILL_ARGB = 0xFF0A0014.toInt()
private const val PLAQUE_BORDER_ARGB = 0xFF4E00A7.toInt()

private val GOLD = fixedColor(Color(0xFFFFD700))
private val NEAR_BLACK = fixedColor(Color(0xFF0D0D1B))
private val TEXT_WHITE = fixedColor(Color(0xFFFFFFFF))
private val MUTED_GRAY = fixedColor(Color(0xFF8A8A8A))
private val SOLVED_GREEN = fixedColor(Color(0xFF4A7C3F))
private val IN_PROGRESS_ORANGE = fixedColor(Color(0xFFFF8C00))
private val NOT_PLAYED_RED = fixedColor(Color(0xFFC41E3A))

private data class WidgetScale(val width: Float, val height: Float, val visual: Float)

private const val TEXT_SIZE_BOOST = 1.35f

private fun Dp.scaled(scale: Float): Dp = (this.value * scale).dp
private fun TextUnit.scaled(scale: Float): TextUnit = (this.value * scale * TEXT_SIZE_BOOST).sp
private fun dpToPx(dp: Dp, density: Float): Int = (dp.value * density).roundToInt().coerceAtLeast(1)

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
  val drawable = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    setColor(fillColor)
    cornerRadii = floatArrayOf(
      topLeftRadius, topLeftRadius,
      topRightRadius, topRightRadius,
      bottomRightRadius, bottomRightRadius,
      bottomLeftRadius, bottomLeftRadius,
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

  override suspend fun provideGlance(context: Context, id: GlanceId) {
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
private fun DailyWidgetContent(context: Context, data: DailyWidgetData?) {
  val size = LocalSize.current
  val widthScale = size.width.value / REFERENCE_WIDTH.value
  val heightScale = size.height.value / REFERENCE_HEIGHT.value
  val scale = WidgetScale(width = widthScale, height = heightScale, visual = sqrt(widthScale * heightScale))
  val borderThickness = 3.dp.scaled(scale.visual)
  val innerWidth = size.width - borderThickness * 2
  val innerHeight = size.height - borderThickness * 2

  Box(
    modifier = GlanceModifier
      .fillMaxSize()
      .cornerRadius(16.dp.scaled(scale.visual))
      .background(GOLD)
      .clickable(actionStartActivity(openDailyIntent(context))),
  ) {
    Box(
      modifier = GlanceModifier
        .fillMaxSize()
        .padding(horizontal = borderThickness, vertical = borderThickness)
        .cornerRadius(13.dp.scaled(scale.visual)),
    ) {
      if (data == null) {
        EmptyState(context, scale)
      } else {
        val isFresh = data.date == currentDailyDateUtc()
        PanelContent(context, data, isFresh, scale, innerWidth, innerHeight)
      }
    }
  }
}

@Composable
private fun EmptyState(context: Context, scale: WidgetScale) {
  Box(
    modifier = GlanceModifier
      .fillMaxSize()
      .background(NEAR_BLACK),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = context.getString(R.string.widget_empty_state),
      style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 6.sp.scaled(scale.visual), color = GOLD),
    )
  }
}

@Composable
private fun PanelContent(
  context: Context,
  data: DailyWidgetData,
  isFresh: Boolean,
  scale: WidgetScale,
  innerWidth: Dp,
  innerHeight: Dp,
) {
  val leftZoneWidth = innerWidth * (50f / 174f)
  val gapWidth = innerWidth * (3f / 174f)
  val rightZoneWidth = innerWidth - leftZoneWidth - gapWidth
  Row(modifier = GlanceModifier.fillMaxSize()) {
    DailyPlaque(context, data, scale, leftZoneWidth, innerHeight)
    Spacer(modifier = GlanceModifier.width(gapWidth).fillMaxHeight())
    StatsZone(context, data, isFresh, scale, rightZoneWidth, innerHeight)
  }
}

@Composable
private fun DailyPlaque(context: Context, data: DailyWidgetData, scale: WidgetScale, zoneWidth: Dp, zoneHeight: Dp) {
  val density = context.resources.displayMetrics.density
  val zoneCornerPx = dpToPx(6.dp.scaled(scale.visual), density).toFloat()
  val outerCornerPx = dpToPx(13.dp.scaled(scale.visual), density).toFloat()
  val zoneBitmap = roundedRectBitmap(
    widthPx = dpToPx(zoneWidth, density),
    heightPx = dpToPx(zoneHeight, density),
    fillColor = SLATE_ARGB,
    topLeftRadius = outerCornerPx,
    topRightRadius = zoneCornerPx,
    bottomRightRadius = zoneCornerPx,
    bottomLeftRadius = outerCornerPx,
  )

  Box(
    modifier = GlanceModifier
      .width(zoneWidth)
      .height(zoneHeight)
      .background(ImageProvider(zoneBitmap)),
    contentAlignment = Alignment.Center,
  ) {
    val plaqueWidth = zoneWidth * (40f / 50f)
    val plaqueHeight = zoneHeight * (28f / 34f)
    val plaqueCornerPx = dpToPx(6.dp.scaled(scale.visual), density).toFloat()
    val plaqueStrokePx = dpToPx(2.dp.scaled(scale.visual), density)
    val plaqueBitmap = roundedRectBitmap(
      widthPx = dpToPx(plaqueWidth, density),
      heightPx = dpToPx(plaqueHeight, density),
      fillColor = PLAQUE_FILL_ARGB,
      topLeftRadius = plaqueCornerPx,
      topRightRadius = plaqueCornerPx,
      bottomRightRadius = plaqueCornerPx,
      bottomLeftRadius = plaqueCornerPx,
      strokeWidthPx = plaqueStrokePx,
      strokeColor = PLAQUE_BORDER_ARGB,
    )

    Box(
      modifier = GlanceModifier
        .width(plaqueWidth)
        .height(plaqueHeight)
        .background(ImageProvider(plaqueBitmap)),
      contentAlignment = Alignment.Center,
    ) {
      Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
        Text(
          text = context.getString(R.string.widget_daily_label),
          style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 4.sp.scaled(scale.visual), color = GOLD),
          maxLines = 1,
        )
        Text(
          text = context.getString(R.string.widget_daily_number, data.dailyNumber),
          style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 9.sp.scaled(scale.visual), color = TEXT_WHITE),
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun StatsZone(
  context: Context,
  data: DailyWidgetData,
  isFresh: Boolean,
  scale: WidgetScale,
  zoneWidth: Dp,
  zoneHeight: Dp,
) {
  val density = context.resources.displayMetrics.density
  val zoneCornerPx = dpToPx(6.dp.scaled(scale.visual), density).toFloat()
  val outerCornerPx = dpToPx(13.dp.scaled(scale.visual), density).toFloat()
  val zoneBitmap = roundedRectBitmap(
    widthPx = dpToPx(zoneWidth, density),
    heightPx = dpToPx(zoneHeight, density),
    fillColor = NEAR_BLACK_ARGB,
    topLeftRadius = zoneCornerPx,
    topRightRadius = outerCornerPx,
    bottomRightRadius = outerCornerPx,
    bottomLeftRadius = zoneCornerPx,
  )

  Box(
    modifier = GlanceModifier
      .width(zoneWidth)
      .height(zoneHeight)
      .background(ImageProvider(zoneBitmap))
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

@Composable
private fun StreakRow(context: Context, data: DailyWidgetData, scale: WidgetScale) {
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
      style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 7.sp.scaled(scale.visual), color = GOLD),
      maxLines = 1,
    )
    Spacer(modifier = GlanceModifier.defaultWeight())
    Text(
      text = context.getString(R.string.widget_best_streak, data.bestStreak),
      style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 5.5f.sp.scaled(scale.visual), color = MUTED_GRAY),
      maxLines = 1,
    )
  }
}

private data class StatusInfo(val text: String, val color: ColorProvider, val fontSize: TextUnit)

private fun statusInfo(context: Context, data: DailyWidgetData, isFresh: Boolean, scale: WidgetScale): StatusInfo {
  if (isFresh && data.hasPlayedToday) {
    return if (data.wonToday == true) {
      val count = data.guessCount ?: 0
      StatusInfo(
        text = context.resources.getQuantityString(R.plurals.widget_solved_in_guesses, count, count),
        color = SOLVED_GREEN,
        fontSize = 6.sp.scaled(scale.visual),
      )
    } else {
      StatusInfo(context.getString(R.string.widget_game_lost), NOT_PLAYED_RED, 6.sp.scaled(scale.visual))
    }
  }
  if (isFresh && data.inProgress) {
    return StatusInfo(context.getString(R.string.widget_in_progress), IN_PROGRESS_ORANGE, 6.sp.scaled(scale.visual))
  }
  return StatusInfo(context.getString(R.string.widget_not_played_yet), NOT_PLAYED_RED, 5.5f.sp.scaled(scale.visual))
}

private fun rankInfo(context: Context, rank: DailyWidgetRank, scale: WidgetScale): Pair<String, TextUnit> =
  when (rank.status) {
    DailyWidgetRankStatus.RANKED -> {
      val position = rank.rank ?: 0
      val total = rank.outOf ?: 0
      if (total > 0 && position <= 8) {
        context.getString(R.string.widget_rank_top8) to 5.5f.sp.scaled(scale.visual)
      } else {
        val fontSize = if (position >= 1000) 4.5f.sp.scaled(scale.visual) else 5.5f.sp.scaled(scale.visual)
        context.getString(R.string.widget_rank_number, position) to fontSize
      }
    }
    DailyWidgetRankStatus.NO_USERNAME,
    DailyWidgetRankStatus.GUEST -> "???" to 5.5f.sp.scaled(scale.visual)
  }

@Composable
private fun StatusRow(context: Context, data: DailyWidgetData, isFresh: Boolean, scale: WidgetScale) {
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