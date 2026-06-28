package com.utakatalp.donebot.data.overlay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.utakatalp.donebot.R

internal object OverlayServiceChannel {
    const val CHANNEL_ID = "alarm_overlay_fg_channel"
    const val FOREGROUND_NOTIFICATION_ID = 4244

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.overlay_running_channel_name),
            NotificationManager.IMPORTANCE_MIN,
        ).apply {
            description = context.getString(R.string.overlay_running_channel_description)
            enableVibration(false)
            setShowBadge(false)
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }
}
