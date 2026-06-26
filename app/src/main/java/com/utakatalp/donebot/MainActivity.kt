package com.utakatalp.donebot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.utakatalp.donebot.navigation.AuthNavHost
import com.utakatalp.donebot.navigation.MainNavHost
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoneBotTheme {
                var isAuthenticated by rememberSaveable { mutableStateOf(false) }

                if (isAuthenticated) {
                    MainNavHost(onLogout = { isAuthenticated = false })
                } else {
                    AuthNavHost(onAuthenticated = { isAuthenticated = true })
                }
            }
        }
    }
}
