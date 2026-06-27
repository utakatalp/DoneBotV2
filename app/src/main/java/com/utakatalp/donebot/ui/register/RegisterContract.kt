package com.utakatalp.donebot.ui.register

import androidx.compose.runtime.Immutable

object RegisterContract {
    @Immutable
    data class UiState(
        val fullName: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isPasswordVisible: Boolean = false,
        val emailError: RegisterError? = null,
        val passwordError: RegisterError? = null,
        val confirmPasswordError: RegisterError? = null,
        val generalError: RegisterError? = null,
        val passwordStrength: PasswordStrength? = null,
        val isLoading: Boolean = false,
        val isRedirecting: Boolean = false,
    )

    sealed interface UiAction {
        data class OnFullNameChange(val value: String) : UiAction
        data class OnEmailChange(val value: String) : UiAction
        data class OnPasswordChange(val value: String) : UiAction
        data class OnConfirmPasswordChange(val value: String) : UiAction
        data object OnPasswordVisibilityTap : UiAction
        data object OnSignUpTap : UiAction
        data object OnLoginTap : UiAction
    }

    sealed interface UiEffect {
        data class ShowToast(val message: String) : UiEffect
    }

    data class RegisterError(val message: String)

    enum class PasswordStrength { WEAK, MEDIUM, STRONG }
}
