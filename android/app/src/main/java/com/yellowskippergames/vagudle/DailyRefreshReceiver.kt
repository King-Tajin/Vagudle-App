package com.yellowskippergames.vagudle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

private const val RANK_ENDPOINT = "https://vagudle.king-tajin.dev/api/daily-leaderboard-rank"

class DailyRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rolloverIfNeeded(appContext)
                refreshRank(appContext)
            } catch (_: Exception) {
            } finally {
                scheduleDailyRefresh(appContext)
                pendingResult.finish()
            }
        }
    }
}

private fun rolloverIfNeeded(context: Context) {
    val previous = loadDailyWidgetData(context) ?: return
    val today = currentDailyDateUtc()
    if (previous.date == today) return
    val prefs = context.getSharedPreferences(DAILY_WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
    saveDailyWidgetData(prefs, rolledOverDailyWidgetData(previous, today))
    requestDailyWidgetUpdate(context)
}

private fun refreshRank(context: Context) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val idToken =
        try {
            Tasks.await(user.getIdToken(false), 10, TimeUnit.SECONDS)?.token
        } catch (_: Exception) {
            null
        } ?: return

    val rank = fetchDailyLeaderboardRank(idToken) ?: return
    val current = loadDailyWidgetData(context) ?: return
    val prefs = context.getSharedPreferences(DAILY_WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
    saveDailyWidgetData(prefs, current.copy(rank = rank))
    requestDailyWidgetUpdate(context)
}

private fun fetchDailyLeaderboardRank(idToken: String): DailyWidgetRank? {
    var connection: HttpURLConnection? = null
    return try {
        connection =
            (URL(RANK_ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $idToken")
                connectTimeout = 8000
                readTimeout = 8000
            }
        if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(body)
        if (!json.optBoolean("success", false)) return null
        if (!json.optBoolean("linked", false)) return DailyWidgetRank(status = DailyWidgetRankStatus.GUEST)
        if (!json.optBoolean("hasUsername", false)) {
            return DailyWidgetRank(status = DailyWidgetRankStatus.NO_USERNAME)
        }
        DailyWidgetRank(
            status = DailyWidgetRankStatus.RANKED,
            rank = json.optInt("rank"),
            outOf = json.optInt("outOf"),
        )
    } catch (_: Exception) {
        null
    } finally {
        connection?.disconnect()
    }
}
