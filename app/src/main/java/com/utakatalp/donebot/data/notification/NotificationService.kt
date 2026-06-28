package com.utakatalp.donebot.data.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.utakatalp.donebot.MainActivity
import com.utakatalp.donebot.R
import com.utakatalp.donebot.common.RingtoneHolder
import com.utakatalp.donebot.ui.overlay.OverlayServiceChannel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationService : Service() {

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private val ringtone = RingtoneHolder()

    override fun onCreate() {
        super.onCreate()
        OverlayServiceChannel.ensure(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        promoteToForeground()
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        val minutesBefore = intent.getLongExtra(EXTRA_MINUTES_BEFORE, 0L)
        if (!message.isNullOrBlank()) {
            postReminder(message, minutesBefore.toInt())
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
        return START_NOT_STICKY
    }

    private fun postReminder(contentText: String, minutesBefore: Int) {
        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            REMINDER_NOTIFICATION_ID,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )
        val title = if (minutesBefore == 0) {
            getString(R.string.notification_reminder_title_now)
        } else {
            getString(R.string.notification_reminder_title_in_minutes, minutesBefore)
        }
        val notification = NotificationCompat.Builder(this, OverlayServiceChannel.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(REMINDER_NOTIFICATION_ID, notification)
        ringtone.play(context = this, explicitUri = null)
    }

    private fun promoteToForeground() {
        val placeholder: Notification = NotificationCompat
            .Builder(this, OverlayServiceChannel.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    OverlayServiceChannel.FOREGROUND_NOTIFICATION_ID,
                    placeholder,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(OverlayServiceChannel.FOREGROUND_NOTIFICATION_ID, placeholder)
            }
        }
    }

    override fun onDestroy() {
        ringtone.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        private const val REMINDER_NOTIFICATION_ID = 1
    }
}
