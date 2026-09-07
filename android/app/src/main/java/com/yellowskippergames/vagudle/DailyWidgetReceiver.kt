package com.yellowskippergames.vagudle

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DailyWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = DailyWidget()

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    scheduleDailyRefresh(context)
  }

  override fun onDisabled(context: Context) {
    super.onDisabled(context)
    cancelDailyRefresh(context)
  }
}