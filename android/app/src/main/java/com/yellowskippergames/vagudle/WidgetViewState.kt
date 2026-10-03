package com.yellowskippergames.vagudle

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class WidgetViewState<T>(
    val data: T?,
    val setupFailed: Boolean,
)

internal data class WidgetMessage(
    val title: String,
    val subtitle: String,
    val isError: Boolean,
)

internal fun widgetMessage(
    context: Context,
    setupFailed: Boolean,
): WidgetMessage =
    if (setupFailed) {
        WidgetMessage(
            title = context.getString(R.string.widget_setup_retry),
            subtitle = context.getString(R.string.widget_setup_retry_subtitle),
            isError = true,
        )
    } else {
        WidgetMessage(
            title = context.getString(R.string.widget_empty_state),
            subtitle = context.getString(R.string.widget_empty_state_subtitle),
            isError = false,
        )
    }

internal fun <T> loadWidgetViewState(
    context: Context,
    kind: WidgetKind,
    loadData: (Context) -> T?,
): WidgetViewState<T> =
    WidgetViewState(
        data = loadData(context),
        setupFailed = hasWidgetSyncFailed(kind.prefs(context)),
    )

internal fun <T> widgetViewStateUpdates(
    context: Context,
    kind: WidgetKind,
    loadData: (Context) -> T?,
): Flow<WidgetViewState<T>> =
    callbackFlow {
        val prefs = kind.prefs(context)
        trySend(loadWidgetViewState(context, kind, loadData))
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                trySend(loadWidgetViewState(context, kind, loadData))
            }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
