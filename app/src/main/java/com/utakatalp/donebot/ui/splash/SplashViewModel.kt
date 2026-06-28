package com.utakatalp.donebot.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.common.DomainException
import com.utakatalp.donebot.domain.repository.AuthRepository
import com.utakatalp.donebot.domain.repository.SessionPreferences
import com.utakatalp.donebot.domain.repository.TaskRepository
import com.utakatalp.donebot.domain.repository.TaskSyncRepository
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
    private val sessionPreferences: SessionPreferences,
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val taskSyncRepository: TaskSyncRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Resolving)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = resolve()
        }
    }

    fun onAction(action: UiAction) {
        when (action) {
            UiAction.OnAuthenticated -> _uiState.value = UiState.EnterApp
            UiAction.OnCancelAuth -> _uiState.value = UiState.EnterApp
            UiAction.OnLoggedOut -> viewModelScope.launch {
                taskRepository.clearAll()
                sessionPreferences.clear()
                _uiState.value = UiState.NeedsAuth()
            }
            is UiAction.OnRequestAuth -> {
                _uiState.value = UiState.NeedsAuth(startAt = action.startAt, cancelable = true)
            }
        }
    }

    private suspend fun resolve(): UiState {
        val refreshToken = sessionPreferences.getRefreshToken()
        val accessToken = sessionPreferences.getAccessToken()
        val expiresAt = sessionPreferences.getExpiresAt()
        val now = System.currentTimeMillis()

        return when {
            refreshToken.isNullOrBlank() -> {
                if (!accessToken.isNullOrBlank()) sessionPreferences.clear()
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
                sessionPreferences.saveSession(session)
                taskSyncRepository.syncPendingTasks()
                UiState.EnterApp
            },
            onFailure = { error ->
                when (error) {
                    is DomainException.Unauthorized -> {
                        sessionPreferences.clear()
                        UiState.NeedsAuth()
                    }
                    else -> UiState.EnterApp
                }
            },
        )
}
