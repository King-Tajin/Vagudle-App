package com.yellowskippergames.vagudle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.net.toUri

private const val KEY_WIDGET_FIRST_SYNC_COMPLETED = "widgetFirstSyncCompleted"
private const val KEY_WIDGET_LAST_SYNC_FAILED_AT = "widgetLastSyncFailedAt"

const val EXTRA_QUICK_WIDGET_SETUP = "quickWidgetSetup"
const val EXTRA_QUICK_WIDGET_SETUP_KIND = "quickWidgetSetupKind"
const val QUICK_WIDGET_SETUP_TIMEOUT_MS = 10_000L
const val QUICK_WIDGET_SETUP_SETTLE_DELAY_MS = 600L

fun isWidgetFirstSyncCompleted(prefs: SharedPreferences): Boolean =
    prefs.getBoolean(KEY_WIDGET_FIRST_SYNC_COMPLETED, false)

fun markWidgetFirstSyncCompleted(prefs: SharedPreferences) {
    prefs.edit {
        putBoolean(KEY_WIDGET_FIRST_SYNC_COMPLETED, true)
        remove(KEY_WIDGET_LAST_SYNC_FAILED_AT)
    }
}

fun hasWidgetSyncFailed(prefs: SharedPreferences): Boolean = prefs.contains(KEY_WIDGET_LAST_SYNC_FAILED_AT)

fun markWidgetSyncFailed(prefs: SharedPreferences) {
    prefs.edit { putLong(KEY_WIDGET_LAST_SYNC_FAILED_AT, System.currentTimeMillis()) }
}

fun clearWidgetSyncFailure(prefs: SharedPreferences) {
    prefs.edit { remove(KEY_WIDGET_LAST_SYNC_FAILED_AT) }
}

fun markWidgetSetupFailed(
    context: Context,
    widgetKey: String?,
) {
    WidgetKind.fromKey(widgetKey)?.let { markWidgetSyncFailed(it.prefs(context)) }
}

fun clearWidgetSetupFailure(
    context: Context,
    widgetKey: String?,
) {
    WidgetKind.fromKey(widgetKey)?.let { clearWidgetSyncFailure(it.prefs(context)) }
}

internal fun openWidgetIntent(
    context: Context,
    kind: WidgetKind,
    deepLinkUrl: String? = null,
): Intent {
    val needsQuickSetup = !isWidgetFirstSyncCompleted(kind.prefs(context))
    val launchIntent =
        if (deepLinkUrl == null) context.packageManager.getLaunchIntentForPackage(context.packageName) else null
    val baseIntent =
        launchIntent ?: Intent(Intent.ACTION_VIEW, (deepLinkUrl ?: DEEP_LINK_URL).toUri()).apply {
            setPackage(context.packageName)
        }
    return baseIntent.apply {
        if (needsQuickSetup) {
            putExtra(EXTRA_QUICK_WIDGET_SETUP, true)
            putExtra(EXTRA_QUICK_WIDGET_SETUP_KIND, kind.key)
        }
    }
}
