package com.utakatalp.donebot.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.repository.AuthSessionRepository
import com.utakatalp.donebot.navigation.Login
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.navigation.Register
import com.utakatalp.donebot.ui.profile.ProfileContract.UiAction
import com.utakatalp.donebot.ui.profile.ProfileContract.UiEffect
import com.utakatalp.donebot.ui.profile.ProfileContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    authSession: AuthSessionRepository,
) : ViewModel() {

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _showLogoutDialog = MutableStateFlow(false)

    val uiState: StateFlow<UiState> = combine(
        authSession.observeRefreshToken().map { !it.isNullOrBlank() },
        _showLogoutDialog,
    ) { isAuthenticated, showLogoutDialog ->
        UiState(isAuthenticated = isAuthenticated, showLogoutDialog = showLogoutDialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun onAction(action: UiAction) {
        when (action) {
            UiAction.OnLoginTap -> _navEffect.trySend(NavigationEffect.Navigate(Login))
            UiAction.OnRegisterTap -> _navEffect.trySend(NavigationEffect.Navigate(Register))
            UiAction.OnLogoutTap -> _showLogoutDialog.value = true
            UiAction.OnLogoutDismiss -> _showLogoutDialog.value = false
            UiAction.OnLogoutConfirm -> {
                _showLogoutDialog.value = false
                _uiEffect.trySend(UiEffect.Logout)
            }
        }
    }
}
