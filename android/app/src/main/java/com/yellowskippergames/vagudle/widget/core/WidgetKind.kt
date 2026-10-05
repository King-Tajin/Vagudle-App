package com.yellowskippergames.vagudle.widget.core

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.yellowskippergames.vagudle.AchievementsWidgetReceiver
import com.yellowskippergames.vagudle.DailyWidgetReceiver
import com.yellowskippergames.vagudle.StatsWidgetReceiver
import com.yellowskippergames.vagudle.widget.achievements.ACHIEVEMENTS_WIDGET_PREFS_NAME
import com.yellowskippergames.vagudle.widget.achievements.AchievementsWidget
import com.yellowskippergames.vagudle.widget.achievements.AchievementsWidgetHandler
import com.yellowskippergames.vagudle.widget.daily.DAILY_WIDGET_PREFS_NAME
import com.yellowskippergames.vagudle.widget.daily.DailyWidget
import com.yellowskippergames.vagudle.widget.daily.DailyWidgetHandler
import com.yellowskippergames.vagudle.widget.stats.STATS_WIDGET_PREFS_NAME
import com.yellowskippergames.vagudle.widget.stats.StatsWidget
import com.yellowskippergames.vagudle.widget.stats.StatsWidgetHandler
import org.json.JSONObject

private const val WEB_ORIGIN = "https://vagudle.king-tajin.dev"

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
    val deepLinkUrl: String,
    private val prefsName: String,
    private val receiverClass: Class<out GlanceAppWidgetReceiver>,
    private val widgetFactory: () -> GlanceAppWidget,
    internal val handler: WidgetHandler,
) {
    DAILY(
        key = "daily",
        deepLinkUrl = "$WEB_ORIGIN/daily",
        prefsName = DAILY_WIDGET_PREFS_NAME,
        receiverClass = DailyWidgetReceiver::class.java,
        widgetFactory = { DailyWidget() },
        handler = DailyWidgetHandler,
    ),
    ACHIEVEMENTS(
        key = "achievements",
        deepLinkUrl = "$WEB_ORIGIN/?achievement=",
        prefsName = ACHIEVEMENTS_WIDGET_PREFS_NAME,
        receiverClass = AchievementsWidgetReceiver::class.java,
        widgetFactory = { AchievementsWidget() },
        handler = AchievementsWidgetHandler,
    ),
    STATS(
        key = "stats",
        deepLinkUrl = "$WEB_ORIGIN/?stats=1",
        prefsName = STATS_WIDGET_PREFS_NAME,
        receiverClass = StatsWidgetReceiver::class.java,
        widgetFactory = { StatsWidget() },
        handler = StatsWidgetHandler,
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
