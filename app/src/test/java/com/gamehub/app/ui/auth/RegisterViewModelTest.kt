package com.gamehub.app.ui.auth

import com.gamehub.app.data.remote.ApiError
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.testing.FakeAuthRepository
import com.gamehub.app.testing.MainDispatcherRule
import com.gamehub.app.utils.FieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()

    private fun createViewModel() = RegisterViewModel(repository)

    /** Fills the form with valid values so each test only changes what it cares about. */
    private fun RegisterViewModel.fillValidForm() {
        onUsernameChange("Gamer_01")
        onEmailChange("player@example.com")
        onPasswordChange("secret123")
        onConfirmPasswordChange("secret123")
    }

    @Test
    fun emptyForm_showsErrorsOnEveryField_andNeverCallsTheServer() {
        val viewModel = createViewModel()

        viewModel.register()

        val state = viewModel.uiState.value
        assertEquals(FieldError.REQUIRED, state.usernameError)
        assertEquals(FieldError.REQUIRED, state.emailError)
        assertEquals(FieldError.REQUIRED, state.passwordError)
        assertEquals(FieldError.REQUIRED, state.confirmPasswordError)
        assertTrue(repository.registerCalls.isEmpty())
    }

    @Test
    fun weakPassword_isRejectedBeforeTheNetworkCall() {
        val viewModel = createViewModel()
        viewModel.fillValidForm()
        viewModel.onPasswordChange("nodigitshere")
        viewModel.onConfirmPasswordChange("nodigitshere")

        viewModel.register()

        assertEquals(FieldError.PASSWORD_NEEDS_NUMBER, viewModel.uiState.value.passwordError)
        assertTrue(repository.registerCalls.isEmpty())
    }

    @Test
    fun mismatchedConfirmation_isReported() {
        val viewModel = createViewModel()
        viewModel.fillValidForm()
        viewModel.onConfirmPasswordChange("different123")

        viewModel.register()

        assertEquals(FieldError.PASSWORDS_DO_NOT_MATCH, viewModel.uiState.value.confirmPasswordError)
        assertTrue(repository.registerCalls.isEmpty())
    }

    @Test
    fun validForm_registers() {
        val viewModel = createViewModel()
        viewModel.fillValidForm()

        viewModel.register()

        assertTrue(viewModel.uiState.value.isRegistered)
        assertEquals(listOf(Triple("Gamer_01", "player@example.com", "secret123")), repository.registerCalls)
    }

    @Test
    fun duplicateEmail_isShownOnTheEmailField() {
        repository.registerResult = ApiResult.Failure(ApiError.EmailTaken)
        val viewModel = createViewModel()
        viewModel.fillValidForm()

        viewModel.register()

        val state = viewModel.uiState.value
        assertEquals(FieldError.EMAIL_TAKEN, state.emailError)
        assertNull(state.error)
        assertFalse(state.isRegistered)
    }

    @Test
    fun duplicateUsername_isShownOnTheUsernameField() {
        repository.registerResult = ApiResult.Failure(ApiError.UsernameTaken)
        val viewModel = createViewModel()
        viewModel.fillValidForm()

        viewModel.register()

        assertEquals(FieldError.USERNAME_TAKEN, viewModel.uiState.value.usernameError)
    }

    @Test
    fun networkProblem_isShownAsAGeneralError() {
        repository.registerResult = ApiResult.Failure(ApiError.NoConnection)
        val viewModel = createViewModel()
        viewModel.fillValidForm()

        viewModel.register()

        assertEquals(ApiError.NoConnection, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}