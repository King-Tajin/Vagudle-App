package com.yellowskippergames.vagudle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.yellowskippergames.vagudle.widget.core.WidgetResizeTipNotifier

class WidgetTipDismissReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        NotificationManagerCompat.from(context).cancel(WidgetResizeTipNotifier.NOTIFICATION_ID)
    }
}
