package com.yellowskippergames.vagudle.widget.daily

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.yellowskippergames.vagudle.R
import com.yellowskippergames.vagudle.widget.ui.GOLD
import com.yellowskippergames.vagudle.widget.ui.PLAQUE_BORDER_ARGB
import com.yellowskippergames.vagudle.widget.ui.PLAQUE_FILL_ARGB
import com.yellowskippergames.vagudle.widget.ui.RoundedZoneBox
import com.yellowskippergames.vagudle.widget.ui.TEXT_WHITE
import com.yellowskippergames.vagudle.widget.ui.WidgetScale
import com.yellowskippergames.vagudle.widget.ui.scaled

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
