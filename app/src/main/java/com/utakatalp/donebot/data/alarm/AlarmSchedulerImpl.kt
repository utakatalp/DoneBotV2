package com.utakatalp.donebot.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.utakatalp.donebot.data.notification.NotificationService
import com.utakatalp.donebot.domain.alarm.AlarmScheduler
import com.utakatalp.donebot.domain.model.AlarmItem
import com.utakatalp.donebot.ui.overlay.OverlayService
import java.time.ZoneId

class AlarmSchedulerImpl(
    private val context: Context,
) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun scheduleForTask(item: AlarmItem) {
        val triggerAtMillis = item.fireAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        scheduleAt(
            triggerAtMillis = triggerAtMillis,
            pendingIntent = buildFirePendingIntent(
                requestCode = taskRequestCode(item.taskId),
                intent = buildPreferredBroadcast(item),
            ),
        )
    }

    override fun cancelForTask(taskId: Long) {
        alarmManager.cancel(
            buildFirePendingIntent(
                requestCode = taskRequestCode(taskId),
                intent = Intent(context, AlarmFireReceiver::class.java),
            ),
        )
    }

    private fun buildFirePendingIntent(requestCode: Int, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            intent.apply { setClass(context, AlarmFireReceiver::class.java) },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun taskRequestCode(taskId: Long): Int = (TASK_REQUEST_BASE + taskId).toInt()

    private fun buildPreferredBroadcast(item: AlarmItem): Intent =
        if (Settings.canDrawOverlays(context)) {
            buildOverlayBroadcast(item)
        } else {
            buildNotificationBroadcast(item)
        }

    private fun buildOverlayBroadcast(item: AlarmItem): Intent =
        Intent(context, AlarmFireReceiver::class.java).apply {
            action = AlarmFireReceiver.ACTION_FIRE
            putExtra(AlarmFireReceiver.EXTRA_FIRE_TARGET, AlarmFireReceiver.FIRE_TARGET_OVERLAY)
            putExtra(OverlayService.EXTRA_MESSAGE, item.message)
            putExtra(OverlayService.EXTRA_MINUTES_BEFORE, item.minutesBefore)
        }

    private fun buildNotificationBroadcast(item: AlarmItem): Intent =
        Intent(context, AlarmFireReceiver::class.java).apply {
            action = AlarmFireReceiver.ACTION_FIRE
            putExtra(AlarmFireReceiver.EXTRA_FIRE_TARGET, AlarmFireReceiver.FIRE_TARGET_NOTIFICATION)
            putExtra(NotificationService.EXTRA_MESSAGE, item.message)
            putExtra(NotificationService.EXTRA_MINUTES_BEFORE, item.minutesBefore)
        }

    private fun scheduleAt(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "scheduleAt: skipping past trigger=$triggerAtMillis")
            return
        }
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }.onFailure { Log.w(TAG, "scheduleAt failed", it) }
    }

    private companion object {
        const val TASK_REQUEST_BASE = 0x0200_0000L
        const val TAG = "AlarmScheduler"
    }
}
