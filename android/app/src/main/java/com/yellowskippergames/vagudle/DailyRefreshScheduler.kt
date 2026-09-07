package com.yellowskippergames.vagudle

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val DAILY_REFRESH_REQUEST_CODE = 4210
private const val DAILY_RELEASE_HOUR_UTC_SCHEDULER = 8L

fun scheduleDailyRefresh(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        nextDailyReleaseInstant().toEpochMilli(),
        dailyRefreshPendingIntent(context),
    )
}

fun cancelDailyRefresh(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    alarmManager.cancel(dailyRefreshPendingIntent(context))
}

private fun dailyRefreshPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, DailyRefreshReceiver::class.java)
    return PendingIntent.getBroadcast(
        context,
        DAILY_REFRESH_REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun nextDailyReleaseInstant(now: Instant = Instant.now()): Instant {
    val todayRelease = now.truncatedTo(ChronoUnit.DAYS).plusSeconds(DAILY_RELEASE_HOUR_UTC_SCHEDULER * 3600)
    return if (now.isBefore(todayRelease)) todayRelease else todayRelease.plus(1, ChronoUnit.DAYS)
}
