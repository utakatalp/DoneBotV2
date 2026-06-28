package com.utakatalp.donebot.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.utakatalp.donebot.data.notification.NotificationService
import com.utakatalp.donebot.data.overlay.OverlayService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmFireReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent ?: return
        val requestedTarget = intent.getStringExtra(EXTRA_FIRE_TARGET).orEmpty()

        // Re-check overlay permission at fire time (the scheduler chose a target when the
        // alarm was armed; the user may have revoked permission since). Routing here keeps
        // OverlayService from being started without permission — if it were, it would have
        // to call startForeground() within 5s anyway to satisfy startForegroundService.
        val effectiveTarget = if (
            requestedTarget == FIRE_TARGET_OVERLAY && !Settings.canDrawOverlays(context)
        ) {
            FIRE_TARGET_NOTIFICATION
        } else {
            requestedTarget
        }
        // Both overlay and notification broadcasts carry the same extra payload (same key
        // strings, different symbolic constants), so a single read works regardless of which
        // target the scheduler picked.
        val message = intent.getStringExtra(OverlayService.EXTRA_MESSAGE).orEmpty()
        val minutesBefore = intent.getLongExtra(OverlayService.EXTRA_MINUTES_BEFORE, 0L)
        Log.d(
            TAG,
            "[AlarmFireReceiver] alarm FIRED requested=$requestedTarget effective=$effectiveTarget " +
                "message='$message' minutesBefore=$minutesBefore",
        )

        val serviceIntent = when (effectiveTarget) {
            FIRE_TARGET_OVERLAY -> Intent(context, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_MESSAGE, message)
                putExtra(OverlayService.EXTRA_MINUTES_BEFORE, minutesBefore)
            }
            FIRE_TARGET_NOTIFICATION -> Intent(context, NotificationService::class.java).apply {
                putExtra(NotificationService.EXTRA_MESSAGE, message)
                putExtra(NotificationService.EXTRA_MINUTES_BEFORE, minutesBefore)
            }
            else -> {
                Log.w(TAG, "Unknown fire target=$requestedTarget; dropping alarm")
                return
            }
        }

        Log.d(TAG, "[AlarmFireReceiver] starting service for target=$effectiveTarget")
        runCatching {
            ContextCompat.startForegroundService(context, serviceIntent)
        }.onFailure {
            Log.d(TAG, "[AlarmFireReceiver] startForegroundService FAILED for target=$effectiveTarget", it)
        }
    }

    companion object {
        const val ACTION_FIRE = "com.utakatalp.donebot.alarm.action.FIRE"
        const val EXTRA_FIRE_TARGET = "com.utakatalp.donebot.alarm.extra.FIRE_TARGET"
        const val FIRE_TARGET_OVERLAY = "OVERLAY"
        const val FIRE_TARGET_NOTIFICATION = "NOTIFICATION"
        private const val TAG = "AlarmFlow"
    }
}
