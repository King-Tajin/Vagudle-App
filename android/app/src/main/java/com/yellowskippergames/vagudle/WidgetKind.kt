package com.yellowskippergames.vagudle

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import org.json.JSONObject

interface WidgetHandler {
    fun sync(
        context: Context,
        payload: JSONObject,
    )

    fun onSynced(context: Context) {}

    fun onBoot(context: Context) {}
}

enum class WidgetKind(
    val key: String,
    private val prefsName: String,
    private val receiverClass: Class<out GlanceAppWidgetReceiver>,
    private val widgetFactory: () -> GlanceAppWidget,
    internal val handler: WidgetHandler,
) {
    DAILY(
        key = "daily",
        prefsName = DAILY_WIDGET_PREFS_NAME,
        receiverClass = DailyWidgetReceiver::class.java,
        widgetFactory = { DailyWidget() },
        handler = DailyWidgetHandler,
    ),
    ACHIEVEMENTS(
        key = "achievements",
        prefsName = ACHIEVEMENTS_WIDGET_PREFS_NAME,
        receiverClass = AchievementsWidgetReceiver::class.java,
        widgetFactory = { AchievementsWidget() },
        handler = AchievementsWidgetHandler,
    ),
    ;

    fun createWidget(): GlanceAppWidget = widgetFactory()

    fun prefs(context: Context): SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun installedWidgetIds(context: Context): IntArray =
        AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, receiverClass))

    companion object {
        fun fromKey(key: String?): WidgetKind? = entries.firstOrNull { it.key == key }
    }
}
