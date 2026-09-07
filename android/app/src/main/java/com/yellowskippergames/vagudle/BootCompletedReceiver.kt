package com.yellowskippergames.vagudle

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val widgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, DailyWidgetReceiver::class.java))
    if (widgetIds.isNotEmpty()) {
      scheduleDailyRefresh(context)
    }
  }
}
