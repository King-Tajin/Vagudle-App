package com.yellowskippergames.vagudle

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.yellowskippergames.vagudle.widget.core.WidgetResizeTipNotifier
import com.yellowskippergames.vagudle.widget.stats.StatsWidget

class StatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StatsWidget()

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
