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

private const val WIDGET_UPDATE_WORK_NAME_PREFIX = "widget_update_"
private const val KEY_WIDGET = "widget"
private const val KEY_REQUESTED_AT = "requestedAt"

class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val kind = WidgetKind.fromKey(inputData.getString(KEY_WIDGET)) ?: return Result.failure()
        return try {
            kind.createWidget().updateAll(applicationContext)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

fun requestWidgetUpdate(
    context: Context,
    kind: WidgetKind,
) {
    val appContext = context.applicationContext
    val request =
        OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setInputData(
                workDataOf(
                    KEY_WIDGET to kind.key,
                    KEY_REQUESTED_AT to System.currentTimeMillis(),
                ),
            ).build()
    WorkManager.getInstance(appContext).enqueueUniqueWork(
        WIDGET_UPDATE_WORK_NAME_PREFIX + kind.key,
        ExistingWorkPolicy.REPLACE,
        request,
    )
}

suspend fun pushWidgetUpdateNow(
    context: Context,
    kind: WidgetKind,
) {
    val appContext = context.applicationContext
    val widget = kind.createWidget()
    val glanceManager = GlanceAppWidgetManager(appContext)
    val appWidgetManager = AppWidgetManager.getInstance(appContext)
    glanceManager.getGlanceIds(widget.javaClass).forEach { glanceId ->
        val appWidgetId = glanceManager.getAppWidgetId(glanceId)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val remoteViews = widget.compose(appContext, glanceId, options = options, size = null, state = null)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }
}

fun updateWidgetNow(
    context: Context,
    kind: WidgetKind,
) {
    runBlocking {
        pushWidgetUpdateNow(context, kind)
    }
}
