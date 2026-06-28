package com.utakatalp.donebot.common

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Handler
import android.os.Looper

class RingtoneHolder {
    private var ringtone: Ringtone? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    fun play(
        context: Context,
        explicitUri: Uri? = null,
        autoStopMillis: Long = AUTO_STOP_MILLIS,
    ) {
        stop()
        stopRunnable?.let(mainHandler::removeCallbacks)
        stopRunnable = null

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager?.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            return
        }

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val uri = explicitUri ?: alarmUri ?: notificationUri

        if (uri == null) {
            ToneGenerator(AudioManager.STREAM_ALARM, TONE_VOLUME)
                .startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, FALLBACK_TONE_MILLIS)
            return
        }

        ringtone = RingtoneManager.getRingtone(context, uri)?.apply {
            runCatching {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            play()
            val autoStop = Runnable { stop() }
            stopRunnable = autoStop
            mainHandler.postDelayed(autoStop, autoStopMillis)
        } ?: run {
            ToneGenerator(AudioManager.STREAM_ALARM, TONE_VOLUME)
                .startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, FALLBACK_TONE_MILLIS)
            null
        }
    }

    fun stop() {
        stopRunnable?.let(mainHandler::removeCallbacks)
        stopRunnable = null
        runCatching { ringtone?.stop() }
        ringtone = null
    }

    private companion object {
        const val AUTO_STOP_MILLIS = 2_000L
        const val FALLBACK_TONE_MILLIS = 800
        const val TONE_VOLUME = 100
    }
}
