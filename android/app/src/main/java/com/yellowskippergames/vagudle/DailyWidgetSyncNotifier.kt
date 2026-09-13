package com.yellowskippergames.vagudle

object DailyWidgetSyncNotifier {
    @Volatile
    private var listener: Runnable? = null

    fun setListener(onSynced: Runnable) {
        listener = onSynced
    }

    fun clearListener() {
        listener = null
    }

    fun notifySynced() {
        listener?.run()
    }
}
