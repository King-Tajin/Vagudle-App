package com.yellowskippergames.vagudle

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val WIDGET_HOST_SETTLE_DELAY_MS = 1500L

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, DailyWidgetReceiver::class.java))
        if (widgetIds.isEmpty()) return

        scheduleDailyRefresh(context)

        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(WIDGET_HOST_SETTLE_DELAY_MS.milliseconds)
                DailyWidget().updateAll(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
