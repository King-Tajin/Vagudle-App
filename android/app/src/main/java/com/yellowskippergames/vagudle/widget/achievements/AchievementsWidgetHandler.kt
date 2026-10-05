package com.yellowskippergames.vagudle.widget.achievements

import android.content.Context
import com.yellowskippergames.vagudle.widget.core.WidgetHandler
import com.yellowskippergames.vagudle.widget.core.WidgetKind
import org.json.JSONObject

internal object AchievementsWidgetHandler : WidgetHandler {
    override fun sync(
        context: Context,
        payload: JSONObject,
    ) {
        saveAchievementsWidgetData(WidgetKind.ACHIEVEMENTS.prefs(context), parseAchievementsWidgetPayload(payload))
    }
}
