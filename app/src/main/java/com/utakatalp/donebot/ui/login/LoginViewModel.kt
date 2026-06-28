package com.utakatalp.donebot.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.model.AuthSession
import com.utakatalp.donebot.domain.repository.SessionPreferences
import com.utakatalp.donebot.domain.repository.TaskSyncRepository
import com.utakatalp.donebot.domain.repository.UserRepository
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
    private val userRepository: UserRepository,
    private val sessionPreferences: SessionPreferences,
    private val taskSyncRepository: TaskSyncRepository,
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
            UiAction.OnLoginTap -> handleLoginTap()
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

    private fun handleLoginTap() {
        val currentState = _uiState.value
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)

        if (emailError == null && passwordError == null) {
            login()
        } else {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    hasSubmittedOnce = true,
                )
            }
        }
    }

    private fun login() = viewModelScope.launch {
        val (email, password) = _uiState.value.let { it.email to it.password }
        _uiState.update { it.copy(isLoading = true, generalError = null) }
        userRepository.login(email, password)
            .onSuccess { onLoginSuccess(it) }
            .onFailure { onLoginFailure(it) }
    }

    private suspend fun onLoginSuccess(session: AuthSession) {
        sessionPreferences.saveSession(session)
        taskSyncRepository.syncPendingTasks()
        _uiState.update { it.copy(isLoading = false) }
        _navEffect.trySend(NavigationEffect.Navigate(Home))
    }

    private fun onLoginFailure(error: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                generalError = LoginError(error.message ?: "Login failed"),
            )
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
