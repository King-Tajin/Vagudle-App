package com.yellowskippergames.vagudle

import android.content.Context
import org.json.JSONObject

internal object StatsWidgetHandler : WidgetHandler {
    override fun sync(
        context: Context,
        payload: JSONObject,
    ) {
        saveStatsWidgetData(WidgetKind.STATS.prefs(context), parseStatsWidgetPayload(payload))
    }
}
