package com.utakatalp.donebot.ui.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.model.AuthSession
import com.utakatalp.donebot.domain.repository.SessionPreferences
import com.utakatalp.donebot.domain.repository.TaskSyncRepository
import com.utakatalp.donebot.domain.repository.UserRepository
import com.utakatalp.donebot.navigation.Home
import com.utakatalp.donebot.navigation.Login
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.ui.register.RegisterContract.PasswordStrength
import com.utakatalp.donebot.ui.register.RegisterContract.RegisterError
import com.utakatalp.donebot.ui.register.RegisterContract.UiAction
import com.utakatalp.donebot.ui.register.RegisterContract.UiEffect
import com.utakatalp.donebot.ui.register.RegisterContract.UiState
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
private const val PASSWORD_STRONG_LENGTH = 12

@HiltViewModel
class RegisterViewModel @Inject constructor(
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

    private var hasSubmittedOnce: Boolean = false

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.OnFullNameChange -> updateFullName(action.value)
            is UiAction.OnEmailChange -> updateEmail(action.value)
            is UiAction.OnPasswordChange -> updatePassword(action.value)
            is UiAction.OnConfirmPasswordChange -> updateConfirmPassword(action.value)
            UiAction.OnPasswordVisibilityTap -> togglePasswordVisibility()
            UiAction.OnSignUpTap -> handleSignUpTap()
            UiAction.OnLoginTap -> _navEffect.trySend(NavigationEffect.Navigate(Login))
        }
    }

    private fun updateFullName(fullName: String) = _uiState.update {
        it.copy(fullName = fullName)
    }

    private fun updateEmail(email: String) = _uiState.update {
        it.copy(
            email = email,
            emailError = if (hasSubmittedOnce) validateEmail(email) else null,
        )
    }

    private fun updatePassword(password: String) = _uiState.update {
        it.copy(
            password = password,
            passwordError = if (hasSubmittedOnce) validatePassword(password) else null,
            passwordStrength = computePasswordStrength(password),
            confirmPasswordError = if (hasSubmittedOnce) {
                validateConfirmPassword(password, it.confirmPassword)
            } else {
                it.confirmPasswordError
            },
        )
    }

    private fun updateConfirmPassword(confirmPassword: String) = _uiState.update {
        it.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = if (hasSubmittedOnce) {
                validateConfirmPassword(it.password, confirmPassword)
            } else {
                null
            },
        )
    }

    private fun togglePasswordVisibility() = _uiState.update {
        it.copy(isPasswordVisible = !it.isPasswordVisible)
    }

    private fun handleSignUpTap() {
        hasSubmittedOnce = true
        val state = _uiState.value
        val fullNameError = validateFullName(state.fullName)
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        val confirmPasswordError = validateConfirmPassword(state.password, state.confirmPassword)

        if (fullNameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    generalError = fullNameError,
                )
            }
            return
        }

        register()
    }

    private fun register() = viewModelScope.launch {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, generalError = null) }
        userRepository.register(
            email = state.email,
            password = state.password,
            displayName = state.fullName,
        )
            .onSuccess { onRegisterSuccess(it) }
            .onFailure { onRegisterFailure(it) }
    }

    private suspend fun onRegisterSuccess(session: AuthSession) {
        sessionPreferences.saveSession(session)
        taskSyncRepository.syncPendingTasks()
        _uiState.update { it.copy(isLoading = false, isRedirecting = true) }
        _navEffect.trySend(NavigationEffect.Navigate(Home))
    }

    private fun onRegisterFailure(error: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                generalError = RegisterError(error.message ?: "Registration failed"),
            )
        }
    }

    private fun validateFullName(fullName: String): RegisterError? = when {
        fullName.isBlank() -> RegisterError("Please enter your full name")
        else -> null
    }

    private fun validateEmail(email: String): RegisterError? = when {
        email.isBlank() -> RegisterError("Email cannot be empty")
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> RegisterError("Please enter a valid email")
        else -> null
    }

    private fun validatePassword(password: String): RegisterError? = when {
        password.isBlank() -> RegisterError("Password cannot be empty")
        password.length < PASSWORD_MIN_LENGTH ->
            RegisterError("Password must be at least $PASSWORD_MIN_LENGTH characters")
        !password.any { it.isDigit() || !it.isLetterOrDigit() } ->
            RegisterError("Password must contain at least one digit or symbol")
        else -> null
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): RegisterError? = when {
        confirmPassword.isBlank() -> RegisterError("Please confirm your password")
        password != confirmPassword -> RegisterError("Passwords do not match")
        else -> null
    }

    private fun computePasswordStrength(password: String): PasswordStrength? {
        if (password.isBlank()) return null
        var score = 0
        score += when {
            password.length >= PASSWORD_STRONG_LENGTH -> 2
            password.length >= PASSWORD_MIN_LENGTH -> 1
            else -> 0
        }
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return when {
            score >= 5 -> PasswordStrength.STRONG
            score >= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
}
