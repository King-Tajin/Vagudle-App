package com.yellowskippergames.vagudle.widget.daily

import android.content.Context
import com.yellowskippergames.vagudle.widget.core.WidgetHandler
import com.yellowskippergames.vagudle.widget.core.WidgetKind
import org.json.JSONObject

internal object DailyWidgetHandler : WidgetHandler {
    override fun sync(
        context: Context,
        payload: JSONObject,
    ) {
        val previousRank = loadDailyWidgetData(context)?.rank
        val data = parseDailyWidgetPayload(payload, previousRank)
        saveDailyWidgetData(WidgetKind.DAILY.prefs(context), data)
    }

    override fun onSynced(context: Context) {
        scheduleDailyRefresh(context)
    }

    override fun onBoot(context: Context) {
        scheduleDailyRefresh(context)
    }
}
