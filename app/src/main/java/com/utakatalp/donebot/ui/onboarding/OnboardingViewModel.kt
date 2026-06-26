package com.utakatalp.donebot.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.utakatalp.donebot.navigation.Home
import com.utakatalp.donebot.navigation.Login
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.ui.onboarding.OnboardingContract.UiAction
import com.utakatalp.donebot.ui.onboarding.OnboardingContract.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val INTERVAL = 1500L

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _navEffect = Channel<NavigationEffect>()
    val navEffect = _navEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(INTERVAL)
                _uiState.update { it.copy(bgIndex = (it.bgIndex + 1) % 4) }
            }
        }
    }

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.OnLoginClick -> _navEffect.trySend(NavigationEffect.Navigate(Login))
            is UiAction.OnGetStartedClick -> _navEffect.trySend(NavigationEffect.Navigate(Home))
        }
    }
}
