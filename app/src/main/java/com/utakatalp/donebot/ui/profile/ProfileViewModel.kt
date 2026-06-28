package com.utakatalp.donebot.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.repository.SessionPreferences
import com.utakatalp.donebot.navigation.Login
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.navigation.Register
import com.utakatalp.donebot.ui.profile.ProfileContract.UiAction
import com.utakatalp.donebot.ui.profile.ProfileContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    sessionPreferences: SessionPreferences,
) : ViewModel() {

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    val uiState: StateFlow<UiState> = sessionPreferences.observeRefreshToken()
        .map { token -> UiState(isAuthenticated = !token.isNullOrBlank()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun onAction(action: UiAction) {
        when (action) {
            UiAction.OnLoginTap -> _navEffect.trySend(NavigationEffect.Navigate(Login))
            UiAction.OnRegisterTap -> _navEffect.trySend(NavigationEffect.Navigate(Register))
        }
    }
}
