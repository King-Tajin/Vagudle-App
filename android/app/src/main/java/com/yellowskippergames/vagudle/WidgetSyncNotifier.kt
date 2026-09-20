package com.yellowskippergames.vagudle

import java.util.function.Consumer

object WidgetSyncNotifier {
    @Volatile
    private var listener: Consumer<String>? = null

    fun setListener(onSynced: Consumer<String>) {
        listener = onSynced
    }

    fun clearListener() {
        listener = null
    }

    fun notifySynced(widgetKey: String) {
        listener?.accept(widgetKey)
    }
}
