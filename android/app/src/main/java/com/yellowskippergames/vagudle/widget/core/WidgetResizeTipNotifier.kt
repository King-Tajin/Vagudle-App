package com.yellowskippergames.vagudle.widget.core

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
import com.yellowskippergames.vagudle.R
import com.yellowskippergames.vagudle.WidgetTipDismissReceiver

private const val PREFS_NAME = "vagudle_widget_tip_prefs"
private const val KEY_RESIZE_TIP_SHOWN = "resize_tip_shown"
private const val KEY_KNOWN_WIDGET_IDS = "known_widget_ids"
private const val CHANNEL_ID = "widget_tips"

object WidgetResizeTipNotifier {
    const val NOTIFICATION_ID = 9001

    fun onWidgetsUpdated(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = appWidgetIds.map { it.toString() }.toSet()
        val known = prefs.getStringSet(KEY_KNOWN_WIDGET_IDS, null)

        if (known == null) {
            prefs.edit { putStringSet(KEY_KNOWN_WIDGET_IDS, current) }
            if (!prefs.getBoolean(KEY_RESIZE_TIP_SHOWN, false)) show(context)
            return
        }

        if ((current - known).isEmpty()) return
        prefs.edit { putStringSet(KEY_KNOWN_WIDGET_IDS, known + current) }
        show(context)
    }

    fun onWidgetsDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val known = prefs.getStringSet(KEY_KNOWN_WIDGET_IDS, null) ?: return
        val removed = appWidgetIds.map { it.toString() }.toSet()
        prefs.edit { putStringSet(KEY_KNOWN_WIDGET_IDS, known - removed) }
    }

    private fun show(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

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
