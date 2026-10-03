package com.yellowskippergames.vagudle

import android.content.Context
import org.json.JSONObject

internal object AchievementsWidgetHandler : WidgetHandler {
    override fun sync(
        context: Context,
        payload: JSONObject,
    ) {
        saveAchievementsWidgetData(WidgetKind.ACHIEVEMENTS.prefs(context), parseAchievementsWidgetPayload(payload))
    }
}
