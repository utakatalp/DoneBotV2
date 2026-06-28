package com.utakatalp.donebot.navigation

import androidx.compose.runtime.Composable
import com.todoapp.uikit.extensions.collectWithLifecycle
import kotlinx.coroutines.flow.Flow

@Composable
fun NavigationEffectController(
    navEffect: Flow<NavigationEffect>,
    onNavigate: (AppKey) -> Unit,
    onBack: () -> Unit = {},
    onReplaceCurrent: (AppKey) -> Unit = onNavigate,
) {
    navEffect.collectWithLifecycle { effect ->
        when (effect) {
            is NavigationEffect.Navigate -> onNavigate(effect.key)
            is NavigationEffect.ReplaceCurrent -> onReplaceCurrent(effect.key)
            NavigationEffect.GoBack -> onBack()
        }
    }
}
