package com.yellowskippergames.vagudle

import android.content.Context
import org.json.JSONObject

fun syncWidget(
    context: Context,
    widgetKey: String?,
    payload: JSONObject?,
) {
    val kind = requireNotNull(WidgetKind.fromKey(widgetKey)) { "Unknown widget: $widgetKey" }
    requireNotNull(payload) { "Missing widget payload." }
    kind.handler.sync(context, payload)
    markWidgetFirstSyncCompleted(kind.prefs(context))
    try {
        updateWidgetNow(context, kind)
    } catch (_: Exception) {
    }
    WidgetSyncNotifier.notifySynced(kind.key)
    try {
        kind.handler.onSynced(context)
    } catch (_: Exception) {
    }
}
