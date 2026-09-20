package com.yellowskippergames.vagudle

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject

const val ACHIEVEMENTS_WIDGET_PREFS_NAME = "vagudle_achievements_widget_prefs"
private const val KEY_UNLOCKED_COUNT = "unlockedCount"
private const val KEY_TOTAL_ACHIEVEMENTS = "totalAchievements"
private const val KEY_NEXT_UP_TITLE = "nextUpTitle"
private const val KEY_NEXT_UP_PROGRESS = "nextUpProgress"
private const val KEY_NEXT_UP_TARGET = "nextUpTarget"

data class AchievementsWidgetData(
    val unlockedCount: Int,
    val totalAchievements: Int,
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
        nextUpTitle = prefs.getString(KEY_NEXT_UP_TITLE, "") ?: "",
        nextUpProgress = if (prefs.contains(KEY_NEXT_UP_PROGRESS)) prefs.getInt(KEY_NEXT_UP_PROGRESS, 0) else null,
        nextUpTarget = if (prefs.contains(KEY_NEXT_UP_TARGET)) prefs.getInt(KEY_NEXT_UP_TARGET, 0) else null,
    )
}

data class AchievementsWidgetViewState(
    val data: AchievementsWidgetData?,
    val setupFailed: Boolean,
)

fun loadAchievementsWidgetViewState(context: Context): AchievementsWidgetViewState =
    AchievementsWidgetViewState(
        data = loadAchievementsWidgetData(context),
        setupFailed = hasWidgetSyncFailed(WidgetKind.ACHIEVEMENTS.prefs(context)),
    )

fun achievementsWidgetViewStateUpdates(context: Context): Flow<AchievementsWidgetViewState> =
    callbackFlow {
        val prefs = WidgetKind.ACHIEVEMENTS.prefs(context)
        trySend(loadAchievementsWidgetViewState(context))
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                trySend(loadAchievementsWidgetViewState(context))
            }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

internal object AchievementsWidgetHandler : WidgetHandler {
    override fun sync(
        context: Context,
        payload: JSONObject,
    ) {
        saveAchievementsWidgetData(WidgetKind.ACHIEVEMENTS.prefs(context), parseAchievementsWidgetPayload(payload))
    }
}
