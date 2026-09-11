package com.yellowskippergames.vagudle

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

const val DAILY_WIDGET_PREFS_NAME = "vagudle_widget_prefs"
private const val KEY_DATE = "date"
private const val KEY_DAILY_NUMBER = "dailyNumber"
private const val KEY_WORD_LENGTH = "wordLength"
private const val KEY_HARD_MODE = "hardMode"
private const val KEY_CURRENT_STREAK = "currentStreak"
private const val KEY_BEST_STREAK = "bestStreak"
private const val KEY_HAS_PLAYED_TODAY = "hasPlayedToday"
private const val KEY_IN_PROGRESS = "inProgress"
private const val KEY_WON_TODAY = "wonToday"
private const val KEY_GUESS_COUNT = "guessCount"
private const val KEY_MAX_GUESSES = "maxGuesses"
private const val KEY_RANK_STATUS = "rankStatus"
private const val KEY_RANK = "rank"
private const val KEY_OUT_OF = "outOf"
private const val KEY_LAST_REFRESH_ATTEMPT_AT = "lastRefreshAttemptAt"

private const val DAILY_RELEASE_HOUR_UTC = 8L

enum class DailyWidgetRankStatus {
    GUEST,
    NO_USERNAME,
    RANKED,
}

data class DailyWidgetRank(
    val status: DailyWidgetRankStatus,
    val rank: Int? = null,
    val outOf: Int? = null,
)

data class DailyWidgetData(
    val date: String,
    val dailyNumber: Int,
    val wordLength: Int,
    val hardMode: Boolean,
    val currentStreak: Int,
    val bestStreak: Int,
    val hasPlayedToday: Boolean,
    val inProgress: Boolean,
    val wonToday: Boolean?,
    val guessCount: Int?,
    val maxGuesses: Int?,
    val rank: DailyWidgetRank,
)

fun currentDailyDateUtc(now: Instant = Instant.now()): String {
    val shifted = now.minusSeconds(DAILY_RELEASE_HOUR_UTC * 3600)
    return DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(shifted)
}

data class DailyRotationEntry(
    val wordLength: Int,
    val hardMode: Boolean,
)

private val DAILY_ROTATION =
    listOf(
        DailyRotationEntry(wordLength = 4, hardMode = false),
        DailyRotationEntry(wordLength = 4, hardMode = true),
        DailyRotationEntry(wordLength = 5, hardMode = false),
        DailyRotationEntry(wordLength = 5, hardMode = true),
        DailyRotationEntry(wordLength = 4, hardMode = false),
        DailyRotationEntry(wordLength = 5, hardMode = true),
        DailyRotationEntry(wordLength = 4, hardMode = true),
    )

private fun rotationForDate(date: LocalDate): DailyRotationEntry {
    val jsWeekday = date.dayOfWeek.value % 7
    return DAILY_ROTATION[jsWeekday]
}

fun rolledOverDailyWidgetData(
    previous: DailyWidgetData,
    newDate: String,
): DailyWidgetData {
    val previousDate = LocalDate.parse(previous.date)
    val nextDate = LocalDate.parse(newDate)
    val daysElapsed = ChronoUnit.DAYS.between(previousDate, nextDate).coerceAtLeast(0)
    val rotation = rotationForDate(nextDate)
    return previous.copy(
        date = newDate,
        dailyNumber = (previous.dailyNumber + daysElapsed).toInt(),
        wordLength = rotation.wordLength,
        hardMode = rotation.hardMode,
        hasPlayedToday = false,
        inProgress = false,
        wonToday = null,
        guessCount = null,
        maxGuesses = null,
    )
}

fun parseDailyWidgetPayload(
    json: JSONObject,
    previousRank: DailyWidgetRank? = null,
): DailyWidgetData {
    val rankJson = json.optJSONObject("rank")
    val rank =
        if (rankJson == null) {
            previousRank ?: DailyWidgetRank(status = DailyWidgetRankStatus.GUEST)
        } else {
            val rankStatus =
                when (rankJson.getString("status")) {
                    "ranked" -> DailyWidgetRankStatus.RANKED
                    "no_username" -> DailyWidgetRankStatus.NO_USERNAME
                    else -> DailyWidgetRankStatus.GUEST
                }
            DailyWidgetRank(
                status = rankStatus,
                rank = if (rankJson.has("rank")) rankJson.getInt("rank") else null,
                outOf = if (rankJson.has("outOf")) rankJson.getInt("outOf") else null,
            )
        }
    return DailyWidgetData(
        date = json.getString("date"),
        dailyNumber = json.getInt("dailyNumber"),
        wordLength = json.getInt("wordLength"),
        hardMode = json.getBoolean("hardMode"),
        currentStreak = json.getInt("currentStreak"),
        bestStreak = json.getInt("bestStreak"),
        hasPlayedToday = json.getBoolean("hasPlayedToday"),
        inProgress = json.optBoolean("inProgress", false),
        wonToday = if (json.isNull("wonToday")) null else json.getBoolean("wonToday"),
        guessCount = if (json.isNull("guessCount")) null else json.getInt("guessCount"),
        maxGuesses = if (json.isNull("maxGuesses")) null else json.getInt("maxGuesses"),
        rank = rank,
    )
}

fun saveDailyWidgetData(
    prefs: SharedPreferences,
    data: DailyWidgetData,
) {
    prefs.edit {
        putString(KEY_DATE, data.date)
        putInt(KEY_DAILY_NUMBER, data.dailyNumber)
        putInt(KEY_WORD_LENGTH, data.wordLength)
        putBoolean(KEY_HARD_MODE, data.hardMode)
        putInt(KEY_CURRENT_STREAK, data.currentStreak)
        putInt(KEY_BEST_STREAK, data.bestStreak)
        putBoolean(KEY_HAS_PLAYED_TODAY, data.hasPlayedToday)
        putBoolean(KEY_IN_PROGRESS, data.inProgress)
        if (data.wonToday == null) remove(KEY_WON_TODAY) else putBoolean(KEY_WON_TODAY, data.wonToday)
        if (data.guessCount == null) remove(KEY_GUESS_COUNT) else putInt(KEY_GUESS_COUNT, data.guessCount)
        if (data.maxGuesses == null) remove(KEY_MAX_GUESSES) else putInt(KEY_MAX_GUESSES, data.maxGuesses)
        putString(KEY_RANK_STATUS, data.rank.status.name)
        if (data.rank.rank == null) remove(KEY_RANK) else putInt(KEY_RANK, data.rank.rank)
        if (data.rank.outOf == null) remove(KEY_OUT_OF) else putInt(KEY_OUT_OF, data.rank.outOf)
    }
}

fun lastRefreshAttemptAt(prefs: SharedPreferences): Long = prefs.getLong(KEY_LAST_REFRESH_ATTEMPT_AT, 0L)

fun markRefreshAttemptNow(prefs: SharedPreferences) {
    prefs.edit { putLong(KEY_LAST_REFRESH_ATTEMPT_AT, System.currentTimeMillis()) }
}

fun loadDailyWidgetData(context: Context): DailyWidgetData? {
    val prefs = context.getSharedPreferences(DAILY_WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
    val date = prefs.getString(KEY_DATE, null) ?: return null
    val rankStatus =
        try {
            DailyWidgetRankStatus.valueOf(prefs.getString(KEY_RANK_STATUS, null) ?: return null)
        } catch (_: IllegalArgumentException) {
            return null
        }
    return DailyWidgetData(
        date = date,
        dailyNumber = prefs.getInt(KEY_DAILY_NUMBER, 0),
        wordLength = prefs.getInt(KEY_WORD_LENGTH, 0),
        hardMode = prefs.getBoolean(KEY_HARD_MODE, false),
        currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0),
        bestStreak = prefs.getInt(KEY_BEST_STREAK, 0),
        hasPlayedToday = prefs.getBoolean(KEY_HAS_PLAYED_TODAY, false),
        inProgress = prefs.getBoolean(KEY_IN_PROGRESS, false),
        wonToday = if (prefs.contains(KEY_WON_TODAY)) prefs.getBoolean(KEY_WON_TODAY, false) else null,
        guessCount = if (prefs.contains(KEY_GUESS_COUNT)) prefs.getInt(KEY_GUESS_COUNT, 0) else null,
        maxGuesses = if (prefs.contains(KEY_MAX_GUESSES)) prefs.getInt(KEY_MAX_GUESSES, 0) else null,
        rank =
            DailyWidgetRank(
                status = rankStatus,
                rank = if (prefs.contains(KEY_RANK)) prefs.getInt(KEY_RANK, 0) else null,
                outOf = if (prefs.contains(KEY_OUT_OF)) prefs.getInt(KEY_OUT_OF, 0) else null,
            ),
    )
}
