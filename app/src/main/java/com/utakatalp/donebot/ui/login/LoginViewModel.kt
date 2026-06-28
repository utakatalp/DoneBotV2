package com.utakatalp.donebot.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.repository.AuthRepository
import com.utakatalp.donebot.domain.repository.AuthSessionRepository
import com.utakatalp.donebot.domain.usecase.FetchTasksUseCase
import com.utakatalp.donebot.navigation.Home
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.navigation.Register
import com.utakatalp.donebot.ui.login.LoginContract.LoginError
import com.utakatalp.donebot.ui.login.LoginContract.UiAction
import com.utakatalp.donebot.ui.login.LoginContract.UiEffect
import com.utakatalp.donebot.ui.login.LoginContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PASSWORD_MIN_LENGTH = 8

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authSession: AuthSessionRepository,
    private val fetchTasksUseCase: FetchTasksUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.OnEmailChange -> updateEmail(action.value)
            is UiAction.OnPasswordChange -> updatePassword(action.value)
            UiAction.OnPasswordVisibilityTap -> togglePasswordVisibility()
            UiAction.OnLoginTap -> tryLogin()
            UiAction.OnForgotPasswordTap -> _uiEffect.trySend(UiEffect.ShowToast("Forgot password not implemented yet"))
            UiAction.OnRegisterTap -> _navEffect.trySend(NavigationEffect.Navigate(Register))
        }
    }

    private fun updateEmail(email: String) = _uiState.update {
        it.copy(
            email = email,
            emailError = if (it.hasSubmittedOnce) validateEmail(email) else null,
        )
    }

    private fun updatePassword(password: String) = _uiState.update {
        it.copy(
            password = password,
            passwordError = if (it.hasSubmittedOnce) validatePassword(password) else null,
        )
    }

    private fun togglePasswordVisibility() = _uiState.update {
        it.copy(isPasswordVisible = !it.isPasswordVisible)
    }

    private fun tryLogin() {
        val current = _uiState.value
        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)
        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    hasSubmittedOnce = true,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            authRepository.login(current.email, current.password)
                .onSuccess { session ->
                    authSession.saveSession(session)
                    // Push any guest-mode pending rows AND pull the user's server-side tasks.
                    // syncPendingTasks() alone only does the push; the user wouldn't see their
                    // existing server tasks until the next foreground or pull-to-refresh.
                    fetchTasksUseCase(force = true)
                    _uiState.update { it.copy(isLoading = false) }
                    _navEffect.trySend(NavigationEffect.Navigate(Home))
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = LoginError(error.message ?: "Login failed"),
                        )
                    }
                }
        }
    }

    private fun validateEmail(email: String): LoginError? = when {
        email.isBlank() -> LoginError("Email cannot be empty")
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> LoginError("Please enter a valid email")
        else -> null
    }

    private fun validatePassword(password: String): LoginError? = when {
        password.isBlank() -> LoginError("Password cannot be empty")
        password.length < PASSWORD_MIN_LENGTH -> LoginError("Password must be at least $PASSWORD_MIN_LENGTH characters")
        else -> null
    }
}
