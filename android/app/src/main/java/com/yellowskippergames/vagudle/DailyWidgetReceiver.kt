package com.yellowskippergames.vagudle

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.yellowskippergames.vagudle.widget.core.WidgetResizeTipNotifier
import com.yellowskippergames.vagudle.widget.daily.DailyWidget
import com.yellowskippergames.vagudle.widget.daily.cancelDailyRefresh
import com.yellowskippergames.vagudle.widget.daily.scheduleDailyRefresh

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

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetResizeTipNotifier.onWidgetsUpdated(context, appWidgetIds)
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        super.onDeleted(context, appWidgetIds)
        WidgetResizeTipNotifier.onWidgetsDeleted(context, appWidgetIds)
    }
}
