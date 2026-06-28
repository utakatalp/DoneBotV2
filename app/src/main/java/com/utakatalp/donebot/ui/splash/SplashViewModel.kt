package com.utakatalp.donebot.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.common.DomainException
import com.utakatalp.donebot.domain.repository.AuthRepository
import com.utakatalp.donebot.domain.repository.AuthSessionRepository
import com.utakatalp.donebot.domain.repository.TaskRepository
import com.utakatalp.donebot.domain.usecase.SyncPendingTasksUseCase
import com.utakatalp.donebot.ui.splash.SplashContract.UiAction
import com.utakatalp.donebot.ui.splash.SplashContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authSession: AuthSessionRepository,
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val syncPendingTasksUseCase: SyncPendingTasksUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Resolving)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = resolve()
        }
        // Reactive top-level routing: if AuthSessionRepository.clear() fires anywhere
        // mid-session (TokenRefreshAuthenticator on a failed refresh, explicit logout,
        // anything that nulls the refresh token), flip out of EnterApp so AppRoot
        // recomposes into AuthNavHost. One-way only — entering Main still requires an
        // explicit OnAuthenticated action from AuthNavHost, so a background token
        // write can't silently jump a guest user into the app.
        viewModelScope.launch {
            authSession.observeRefreshToken().collect { token ->
                if (token.isNullOrBlank() && _uiState.value is UiState.EnterApp) {
                    _uiState.value = UiState.NeedsAuth()
                }
            }
        }
    }

    fun onAction(action: UiAction) {
        when (action) {
            UiAction.OnAuthenticated -> _uiState.value = UiState.EnterApp
            UiAction.OnCancelAuth -> _uiState.value = UiState.EnterApp
            UiAction.OnLoggedOut -> viewModelScope.launch {
                taskRepository.clearAll()
                authSession.clear()
                _uiState.value = UiState.NeedsAuth()
            }
            is UiAction.OnRequestAuth -> {
                _uiState.value = UiState.NeedsAuth(startAt = action.startAt, cancelable = true)
            }
        }
    }

    private suspend fun resolve(): UiState {
        val refreshToken = authSession.getRefreshToken()
        val accessToken = authSession.getAccessToken()
        val expiresAt = authSession.getExpiresAt()
        val now = System.currentTimeMillis()

        return when {
            refreshToken.isNullOrBlank() -> {
                if (!accessToken.isNullOrBlank()) authSession.clear()
                UiState.NeedsAuth()
            }

            !accessToken.isNullOrBlank() && expiresAt != null && expiresAt > now ->
                UiState.EnterApp

            else -> tryRefresh(refreshToken)
        }
    }

    private suspend fun tryRefresh(refreshToken: String): UiState =
        authRepository.refresh(refreshToken).fold(
            onSuccess = { session ->
                authSession.saveSession(session)
                syncPendingTasksUseCase()
                UiState.EnterApp
            },
            onFailure = { error ->
                when (error) {
                    is DomainException.Unauthorized -> {
                        authSession.clear()
                        UiState.NeedsAuth()
                    }
                    else -> UiState.EnterApp
                }
            },
        )
}
