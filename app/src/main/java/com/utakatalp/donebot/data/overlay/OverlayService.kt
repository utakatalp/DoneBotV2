package com.utakatalp.donebot.data.overlay

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.todoapp.uikit.components.TDOverlayNotificationCard
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.MainActivity
import com.utakatalp.donebot.R
import com.utakatalp.donebot.common.RingtoneHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OverlayService :
    Service(),
    LifecycleOwner,
    SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private val ringtone = RingtoneHolder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Suppress("ktlint:standard:backing-property-naming")
    private val _lifecycleRegistry = LifecycleRegistry(this)
    @Suppress("ktlint:standard:backing-property-naming")
    private val _savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle = _lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry = _savedStateRegistryController.savedStateRegistry

    private var overlayView: View? = null
    private var startedAsForeground = false

    override fun onBind(intent: Intent?): IBinder = throw UnsupportedOperationException("Bound mode not supported")

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "[OverlayService] onCreate")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        OverlayServiceChannel.ensure(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val message = intent.getStringExtra(EXTRA_MESSAGE).orEmpty()
        val minutesBefore = intent.getLongExtra(EXTRA_MINUTES_BEFORE, 0L)
        Log.d(TAG, "[OverlayService] onStartCommand message='$message' minutesBefore=$minutesBefore")
        promoteToForeground()
        showOverlay(message, minutesBefore)
        serviceScope.launch { ringtone.play(context = this@OverlayService) }
        return START_NOT_STICKY
    }

    private fun showOverlay(message: String, minutesBefore: Long) {
        if (overlayView != null) {
            Log.d(TAG, "[OverlayService] showOverlay: already showing, ignoring")
            return
        }
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setContent {
                TDTheme {
                    var show by remember { mutableStateOf(true) }
                    LaunchedEffect(show) {
                        if (!show) {
                            delay(HIDE_ANIMATION_DELAY_MS)
                            hideOverlay()
                        }
                    }
                    TDOverlayNotificationCard(
                        message = message,
                        minutesBefore = minutesBefore,
                        show = show,
                        onDismissClick = { show = false },
                        onOpenClick = {
                            show = false
                            openApp()
                        },
                    )
                }
            }
        }
        overlayView = view
        Log.d(TAG, "[OverlayService] showOverlay: adding view to WindowManager")
        runCatching {
            windowManager.addView(view, layoutParams)
        }.onFailure {
            Log.d(TAG, "[OverlayService] showOverlay: addView FAILED", it)
            overlayView = null
            stopSelf()
        }
    }

    private fun hideOverlay() {
        overlayView?.let {
            runCatching { windowManager.removeView(it) }
            overlayView = null
        }
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        ringtone.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        startedAsForeground = false
        stopSelf()
    }

    private fun promoteToForeground() {
        if (startedAsForeground) return
        val notification: Notification = NotificationCompat
            .Builder(this, OverlayServiceChannel.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(getString(R.string.overlay_running))
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
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(OverlayServiceChannel.FOREGROUND_NOTIFICATION_ID, notification)
            }
            startedAsForeground = true
            Log.d(TAG, "[OverlayService] promoteToForeground: success")
        }.onFailure { Log.d(TAG, "[OverlayService] promoteToForeground: FAILED", it) }
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "[OverlayService] onDestroy")
        serviceScope.cancel()
        ringtone.stop()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        private const val HIDE_ANIMATION_DELAY_MS = 300L
        private const val TAG = "AlarmFlow"
    }
}
