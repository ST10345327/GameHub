package com.gamehub.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamehub.app.data.remote.ApiError
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.data.repository.AuthRepository
import com.gamehub.app.utils.AppLogger
import com.gamehub.app.utils.FieldError
import com.gamehub.app.utils.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the register screen shows. */
data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameError: FieldError? = null,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val confirmPasswordError: FieldError? = null,
    val isLoading: Boolean = false,
    val error: ApiError? = null,
    val isRegistered: Boolean = false
)

/**
 * Logic for the register screen: validates every field (8+ characters and a number for the
 * password), asks the server to create the account, and maps "email/username already used"
 * answers onto the matching field.
 */
class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null, error = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, error = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, error = null) }
    }

    fun register() {
        val current = _uiState.value
        if (current.isLoading) return

        val usernameError = Validators.validateUsername(current.username)
        val emailError = Validators.validateEmail(current.email)
        val passwordError = Validators.validateNewPassword(current.password)
        val confirmError = Validators.validatePasswordConfirmation(current.password, current.confirmPassword)

        if (listOf(usernameError, emailError, passwordError, confirmError).any { it != null }) {
            AppLogger.debug("Registration blocked by validation")
            _uiState.update {
                it.copy(
                    usernameError = usernameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.register(current.username, current.email, current.password)) {
                is ApiResult.Success -> {
                    AppLogger.debug("Registration succeeded")
                    _uiState.update { it.copy(isLoading = false, isRegistered = true) }
                }
                is ApiResult.Failure -> {
                    AppLogger.warn("Registration failed: ${result.error}")
                    _uiState.update { state ->
                        when (result.error) {
                            // Duplicates are shown next to the field the user needs to change.
                            ApiError.EmailTaken -> state.copy(isLoading = false, emailError = FieldError.EMAIL_TAKEN)
                            ApiError.UsernameTaken -> state.copy(isLoading = false, usernameError = FieldError.USERNAME_TAKEN)
                            else -> state.copy(isLoading = false, error = result.error)
                        }
                    }
                }
            }
        }
    }
}