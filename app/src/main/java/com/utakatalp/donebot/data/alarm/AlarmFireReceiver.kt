package com.utakatalp.donebot.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.utakatalp.donebot.data.notification.NotificationService
import com.utakatalp.donebot.ui.overlay.OverlayService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmFireReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent ?: return
        val target = intent.getStringExtra(EXTRA_FIRE_TARGET).orEmpty()
        Log.d(TAG, "alarm fired target=$target")

        val serviceIntent = when (target) {
            FIRE_TARGET_OVERLAY -> Intent(context, OverlayService::class.java).apply {
                putExtra(
                    OverlayService.EXTRA_MESSAGE,
                    intent.getStringExtra(OverlayService.EXTRA_MESSAGE).orEmpty(),
                )
                putExtra(
                    OverlayService.EXTRA_MINUTES_BEFORE,
                    intent.getLongExtra(OverlayService.EXTRA_MINUTES_BEFORE, 0L),
                )
            }
            FIRE_TARGET_NOTIFICATION -> Intent(context, NotificationService::class.java).apply {
                putExtra(
                    NotificationService.EXTRA_MESSAGE,
                    intent.getStringExtra(NotificationService.EXTRA_MESSAGE).orEmpty(),
                )
                putExtra(
                    NotificationService.EXTRA_MINUTES_BEFORE,
                    intent.getLongExtra(NotificationService.EXTRA_MINUTES_BEFORE, 0L),
                )
            }
            else -> {
                Log.w(TAG, "Unknown fire target=$target; dropping alarm")
                return
            }
        }

        runCatching {
            ContextCompat.startForegroundService(context, serviceIntent)
        }.onFailure { Log.w(TAG, "startForegroundService failed for target=$target", it) }
    }

    companion object {
        const val ACTION_FIRE = "com.utakatalp.donebot.alarm.action.FIRE"
        const val EXTRA_FIRE_TARGET = "com.utakatalp.donebot.alarm.extra.FIRE_TARGET"
        const val FIRE_TARGET_OVERLAY = "OVERLAY"
        const val FIRE_TARGET_NOTIFICATION = "NOTIFICATION"
        private const val TAG = "AlarmFireReceiver"
    }
}
