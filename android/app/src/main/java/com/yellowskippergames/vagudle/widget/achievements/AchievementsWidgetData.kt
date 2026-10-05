package com.yellowskippergames.vagudle.widget.achievements

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.yellowskippergames.vagudle.widget.core.WidgetKind
import com.yellowskippergames.vagudle.widget.core.WidgetViewState
import com.yellowskippergames.vagudle.widget.core.loadWidgetViewState
import com.yellowskippergames.vagudle.widget.core.widgetViewStateUpdates
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

const val ACHIEVEMENTS_WIDGET_PREFS_NAME = "vagudle_achievements_widget_prefs"
private const val KEY_UNLOCKED_COUNT = "unlockedCount"
private const val KEY_TOTAL_ACHIEVEMENTS = "totalAchievements"
private const val KEY_NEXT_UP_ID = "nextUpId"
private const val KEY_NEXT_UP_TITLE = "nextUpTitle"
private const val KEY_NEXT_UP_PROGRESS = "nextUpProgress"
private const val KEY_NEXT_UP_TARGET = "nextUpTarget"

data class AchievementsWidgetData(
    val unlockedCount: Int,
    val totalAchievements: Int,
    val nextUpId: String,
    val nextUpTitle: String,
    val nextUpProgress: Int?,
    val nextUpTarget: Int?,
) {
    val isComplete: Boolean
        get() = totalAchievements in 1..unlockedCount

    val hasCounter: Boolean
        get() = nextUpProgress != null && nextUpTarget != null && nextUpTarget > 0
}

fun parseAchievementsWidgetPayload(json: JSONObject): AchievementsWidgetData {
    val total = json.getInt("totalAchievements").coerceAtLeast(1)
    val rawTarget = if (json.isNull("nextUpTarget")) null else json.getInt("nextUpTarget")
    val rawProgress = if (json.isNull("nextUpProgress")) null else json.getInt("nextUpProgress")
    val counter =
        if (rawTarget != null && rawTarget > 0 && rawProgress != null) {
            rawProgress.coerceIn(0, rawTarget) to rawTarget
        } else {
            null
        }
    return AchievementsWidgetData(
        unlockedCount = json.getInt("unlockedCount").coerceIn(0, total),
        totalAchievements = total,
        nextUpId = json.optString("nextUpId", ""),
        nextUpTitle = json.optString("nextUpTitle", ""),
        nextUpProgress = counter?.first,
        nextUpTarget = counter?.second,
    )
}

fun saveAchievementsWidgetData(
    prefs: SharedPreferences,
    data: AchievementsWidgetData,
) {
    prefs.edit {
        putInt(KEY_UNLOCKED_COUNT, data.unlockedCount)
        putInt(KEY_TOTAL_ACHIEVEMENTS, data.totalAchievements)
        putString(KEY_NEXT_UP_ID, data.nextUpId)
        putString(KEY_NEXT_UP_TITLE, data.nextUpTitle)
        val progress = data.nextUpProgress
        val target = data.nextUpTarget
        if (progress == null) remove(KEY_NEXT_UP_PROGRESS) else putInt(KEY_NEXT_UP_PROGRESS, progress)
        if (target == null) remove(KEY_NEXT_UP_TARGET) else putInt(KEY_NEXT_UP_TARGET, target)
    }
}

fun loadAchievementsWidgetData(context: Context): AchievementsWidgetData? {
    val prefs = WidgetKind.ACHIEVEMENTS.prefs(context)
    if (!prefs.contains(KEY_UNLOCKED_COUNT) || !prefs.contains(KEY_TOTAL_ACHIEVEMENTS)) return null
    return AchievementsWidgetData(
        unlockedCount = prefs.getInt(KEY_UNLOCKED_COUNT, 0),
        totalAchievements = prefs.getInt(KEY_TOTAL_ACHIEVEMENTS, 1),
        nextUpId = prefs.getString(KEY_NEXT_UP_ID, "") ?: "",
        nextUpTitle = prefs.getString(KEY_NEXT_UP_TITLE, "") ?: "",
        nextUpProgress = if (prefs.contains(KEY_NEXT_UP_PROGRESS)) prefs.getInt(KEY_NEXT_UP_PROGRESS, 0) else null,
        nextUpTarget = if (prefs.contains(KEY_NEXT_UP_TARGET)) prefs.getInt(KEY_NEXT_UP_TARGET, 0) else null,
    )
}

typealias AchievementsWidgetViewState = WidgetViewState<AchievementsWidgetData>

fun loadAchievementsWidgetViewState(context: Context): AchievementsWidgetViewState =
    loadWidgetViewState(context, WidgetKind.ACHIEVEMENTS, ::loadAchievementsWidgetData)

fun achievementsWidgetViewStateUpdates(context: Context): Flow<AchievementsWidgetViewState> =
    widgetViewStateUpdates(context, WidgetKind.ACHIEVEMENTS, ::loadAchievementsWidgetData)
