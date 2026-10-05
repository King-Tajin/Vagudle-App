package com.yellowskippergames.vagudle.widget.stats

import android.content.Context
import com.yellowskippergames.vagudle.widget.core.WidgetHandler
import com.yellowskippergames.vagudle.widget.core.WidgetKind
import org.json.JSONObject

internal object StatsWidgetHandler : WidgetHandler {
    override fun sync(
        context: Context,
        payload: JSONObject,
    ) {
        saveStatsWidgetData(WidgetKind.STATS.prefs(context), parseStatsWidgetPayload(payload))
    }
}
