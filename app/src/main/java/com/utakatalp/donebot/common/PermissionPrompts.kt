package com.utakatalp.donebot.common

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.todoapp.uikit.components.TDPermissionPromptCard
import com.utakatalp.donebot.R

@Composable
fun OverlayPermissionPrompt(
    onGranted: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (Settings.canDrawOverlays(context)) onGranted()
    }

    TDPermissionPromptCard(
        icon = Icons.Outlined.Visibility,
        title = stringResource(R.string.permission_overlay_title),
        description = stringResource(R.string.permission_overlay_body),
        ctaText = stringResource(R.string.permission_grant),
        onCtaClick = {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri(),
            )
            launcher.launch(intent)
        },
        onDismiss = onDismiss,
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NotificationPermissionPrompt(
    onGranted: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val permission = Manifest.permission.POST_NOTIFICATIONS
    val shouldOpenSettings = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            shouldOpenSettings.value = false
            onGranted()
        } else {
            val activity = context as? Activity
            val showRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
            } ?: true
            if (!showRationale) shouldOpenSettings.value = true
        }
    }

    TDPermissionPromptCard(
        icon = Icons.Outlined.Notifications,
        title = stringResource(R.string.permission_notification_title),
        description = stringResource(R.string.permission_notification_body),
        ctaText = stringResource(R.string.permission_grant),
        onCtaClick = {
            if (shouldOpenSettings.value) {
                openAppNotificationSettings(context)
            } else {
                launcher.launch(permission)
            }
        },
        onDismiss = onDismiss,
    )
}

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}
