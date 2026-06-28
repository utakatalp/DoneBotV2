package com.utakatalp.donebot.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.navigation.Home
import com.utakatalp.donebot.navigation.Login
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.ui.onboarding.OnboardingContract.UiAction
import com.utakatalp.donebot.ui.onboarding.OnboardingContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val INTERVAL = 1500L
private const val BG_COUNT = 4

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(INTERVAL)
                _uiState.update { it.copy(bgIndex = (it.bgIndex + 1) % BG_COUNT) }
            }
        }
    }

    fun onAction(action: UiAction) {
        when (action) {
            UiAction.OnLoginTap -> _navEffect.trySend(NavigationEffect.Navigate(Login))
            UiAction.OnGetStartedTap -> _navEffect.trySend(NavigationEffect.Navigate(Home))
        }
    }
}
