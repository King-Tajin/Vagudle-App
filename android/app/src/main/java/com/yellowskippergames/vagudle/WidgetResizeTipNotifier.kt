package com.yellowskippergames.vagudle

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.toColorInt

private const val PREFS_NAME = "vagudle_widget_tip_prefs"
private const val KEY_RESIZE_TIP_SHOWN = "resize_tip_shown"
private const val CHANNEL_ID = "widget_tips"

object WidgetResizeTipNotifier {
    const val NOTIFICATION_ID = 9001

    fun maybeShow(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_RESIZE_TIP_SHOWN, false)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        prefs.edit { putBoolean(KEY_RESIZE_TIP_SHOWN, true) }
        ensureChannel(context)

        val acknowledgeIntent = Intent(context, WidgetTipDismissReceiver::class.java)
        val acknowledgePendingIntent =
            PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID,
                acknowledgeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_monochrome)
                .setColor("#EAB308".toColorInt())
                .setContentTitle(context.getString(R.string.widget_resize_tip_title))
                .setContentText(context.getString(R.string.widget_resize_tip_message))
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(context.getString(R.string.widget_resize_tip_message)),
                ).setAutoCancel(true)
                .addAction(0, context.getString(R.string.widget_resize_tip_acknowledge), acknowledgePendingIntent)
                .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.widget_tips_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        channel.description = context.getString(R.string.widget_tips_channel_description)
        manager.createNotificationChannel(channel)
    }
}
