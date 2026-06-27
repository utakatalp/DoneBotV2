package com.utakatalp.donebot.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.previews.TDPreview
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.register.RegisterContract.PasswordStrength
import com.utakatalp.donebot.ui.theme.DoneBotTheme

@Composable
internal fun PasswordStrengthIndicator(strength: PasswordStrength?) {
    strength ?: return
    val (progress, color, label) = strength.toProgress()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = { progress },
            color = color,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row {
            Text(
                text = label.substringBeforeLast(" ") + " ",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label.substringAfterLast(" "),
                style = MaterialTheme.typography.titleSmall,
                color = color,
            )
        }
    }
}

@Composable
private fun PasswordStrength.toProgress(): Triple<Float, Color, String> = when (this) {
    PasswordStrength.STRONG -> Triple(
        1f,
        Color(0xFF2E7D32),
        stringResource(R.string.register_password_strength_strong),
    )
    PasswordStrength.MEDIUM -> Triple(
        0.50f,
        Color(0xFFEF6C00),
        stringResource(R.string.register_password_strength_medium),
    )
    PasswordStrength.WEAK -> Triple(
        0.25f,
        MaterialTheme.colorScheme.error,
        stringResource(R.string.register_password_strength_weak),
    )
}

// region Previews

@TDPreview
@Composable
private fun PasswordStrengthIndicatorAllStatesPreview() {
    DoneBotTheme {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PasswordStrengthIndicator(strength = PasswordStrength.WEAK)
            Spacer(modifier = Modifier.height(4.dp))
            PasswordStrengthIndicator(strength = PasswordStrength.MEDIUM)
            Spacer(modifier = Modifier.height(4.dp))
            PasswordStrengthIndicator(strength = PasswordStrength.STRONG)
        }
    }
}

// endregion
