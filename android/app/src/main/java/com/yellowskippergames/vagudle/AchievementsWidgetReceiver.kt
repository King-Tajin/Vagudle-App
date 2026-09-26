package com.yellowskippergames.vagudle

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class AchievementsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AchievementsWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetResizeTipNotifier.maybeShow(context)
    }
}
