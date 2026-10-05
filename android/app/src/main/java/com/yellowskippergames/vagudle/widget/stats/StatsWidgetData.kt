package com.yellowskippergames.vagudle.widget.stats

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.yellowskippergames.vagudle.widget.core.WidgetKind
import com.yellowskippergames.vagudle.widget.core.WidgetViewState
import com.yellowskippergames.vagudle.widget.core.loadWidgetViewState
import com.yellowskippergames.vagudle.widget.core.widgetViewStateUpdates
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

const val STATS_WIDGET_PREFS_NAME = "vagudle_stats_widget_prefs"
internal const val STATS_BUCKET_COUNT = 6
private const val KEY_NORMAL = "normal"
private const val KEY_HARD = "hard"
private const val NORMAL_FIRST_BUCKET_MAX = 6
private const val HARD_FIRST_BUCKET_MAX = 4

data class StatsModeData(
    val totalGames: Int,
    val successRate: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val firstBucketMax: Int,
    val distribution: List<Int>,
) {
    val mostCommonBucket: Int?
        get() {
            val highest = distribution.maxOrNull() ?: return null
            return if (highest > 0) distribution.indexOf(highest) else null
        }

    companion object {
        fun empty(firstBucketMax: Int) =
            StatsModeData(
                totalGames = 0,
                successRate = 0,
                currentStreak = 0,
                bestStreak = 0,
                firstBucketMax = firstBucketMax,
                distribution = List(STATS_BUCKET_COUNT) { 0 },
            )
    }
}

data class StatsWidgetData(
    val normal: StatsModeData,
    val hard: StatsModeData,
) {
    val totalGames: Int
        get() = normal.totalGames + hard.totalGames
}

internal fun emptyStatsWidgetData() =
    StatsWidgetData(
        normal = StatsModeData.empty(NORMAL_FIRST_BUCKET_MAX),
        hard = StatsModeData.empty(HARD_FIRST_BUCKET_MAX),
    )

private fun parseModeData(json: JSONObject): StatsModeData {
    val buckets = json.getJSONArray("distribution")
    return StatsModeData(
        totalGames = json.getInt("totalGames").coerceAtLeast(0),
        successRate = json.getInt("successRate").coerceIn(0, 100),
        currentStreak = json.getInt("currentStreak").coerceAtLeast(0),
        bestStreak = json.getInt("bestStreak").coerceAtLeast(0),
        firstBucketMax = json.getInt("firstBucketMax").coerceAtLeast(1),
        distribution =
            List(STATS_BUCKET_COUNT) { index ->
                if (index < buckets.length()) buckets.getInt(index).coerceAtLeast(0) else 0
            },
    )
}

private fun StatsModeData.toJson(): JSONObject =
    JSONObject()
        .put("totalGames", totalGames)
        .put("successRate", successRate)
        .put("currentStreak", currentStreak)
        .put("bestStreak", bestStreak)
        .put("firstBucketMax", firstBucketMax)
        .put("distribution", JSONArray(distribution))

fun parseStatsWidgetPayload(json: JSONObject): StatsWidgetData =
    StatsWidgetData(
        normal = parseModeData(json.getJSONObject(KEY_NORMAL)),
        hard = parseModeData(json.getJSONObject(KEY_HARD)),
    )

fun saveStatsWidgetData(
    prefs: SharedPreferences,
    data: StatsWidgetData,
) {
    prefs.edit {
        putString(KEY_NORMAL, data.normal.toJson().toString())
        putString(KEY_HARD, data.hard.toJson().toString())
    }
}

fun loadStatsWidgetData(context: Context): StatsWidgetData? {
    val prefs = WidgetKind.STATS.prefs(context)
    val normal = prefs.getString(KEY_NORMAL, null) ?: return null
    val hard = prefs.getString(KEY_HARD, null) ?: return null
    return try {
        StatsWidgetData(
            normal = parseModeData(JSONObject(normal)),
            hard = parseModeData(JSONObject(hard)),
        )
    } catch (_: Exception) {
        null
    }
}

typealias StatsWidgetViewState = WidgetViewState<StatsWidgetData>

fun loadStatsWidgetViewState(context: Context): StatsWidgetViewState =
    loadWidgetViewState(context, WidgetKind.STATS, ::loadStatsWidgetData)

fun statsWidgetViewStateUpdates(context: Context): Flow<StatsWidgetViewState> =
    widgetViewStateUpdates(context, WidgetKind.STATS, ::loadStatsWidgetData)
