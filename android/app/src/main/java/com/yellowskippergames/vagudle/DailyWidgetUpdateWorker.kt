package com.yellowskippergames.vagudle

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.compose
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking

private const val DAILY_WIDGET_UPDATE_WORK_NAME = "daily_widget_update"
private const val KEY_REQUESTED_AT = "requestedAt"

class DailyWidgetUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        try {
            DailyWidget().updateAll(applicationContext)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
}

fun requestDailyWidgetUpdate(context: Context) {
    val appContext = context.applicationContext
    val request =
        OneTimeWorkRequestBuilder<DailyWidgetUpdateWorker>()
            .setInputData(workDataOf(KEY_REQUESTED_AT to System.currentTimeMillis()))
            .build()
    WorkManager.getInstance(appContext).enqueueUniqueWork(
        DAILY_WIDGET_UPDATE_WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        request,
    )
}

suspend fun pushDailyWidgetUpdateNow(context: Context) {
    val appContext = context.applicationContext
    val widget = DailyWidget()
    val glanceManager = GlanceAppWidgetManager(appContext)
    val appWidgetManager = AppWidgetManager.getInstance(appContext)
    glanceManager.getGlanceIds(DailyWidget::class.java).forEach { glanceId ->
        val appWidgetId = glanceManager.getAppWidgetId(glanceId)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val remoteViews = widget.compose(appContext, glanceId, options = options, size = null, state = null)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }
}

fun updateDailyWidgetNow(context: Context) {
    runBlocking {
        pushDailyWidgetUpdateNow(context)
    }
}
