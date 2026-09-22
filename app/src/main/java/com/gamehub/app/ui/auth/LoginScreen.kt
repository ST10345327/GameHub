package com.gamehub.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamehub.app.R
import com.gamehub.app.ui.common.messageRes
import com.gamehub.app.ui.components.ErrorMessage
import com.gamehub.app.ui.components.LootTextField
import com.gamehub.app.ui.components.LoadingButton

/**
 * Login screen (Part 1, section 4.2). Google Sign-In is a PoE feature and is not included.
 * The screen only draws [LoginUiState] and forwards user actions to the [LoginViewModel].
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate exactly once, when the ViewModel reports a successful login.
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.loot_gamehub),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.auth_welcome_back),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.auth_sign_in_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))

        LootTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = stringResource(R.string.auth_email),
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            errorMessage = state.emailError?.let { stringResource(it.messageRes()) }
        )
        Spacer(Modifier.height(12.dp))
        LootTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(R.string.auth_password),
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            errorMessage = state.passwordError?.let { stringResource(it.messageRes()) }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = viewModel::openForgotPassword) {
                Text(stringResource(R.string.auth_forgot_password))
            }
        }

        // Server/network problems (wrong password, no internet, ...) appear above the button.
        state.error?.let { ErrorMessage(text = stringResource(it.messageRes())) }
        Spacer(Modifier.height(8.dp))

        LoadingButton(
            text = stringResource(R.string.auth_sign_in),
            isLoading = state.isLoading,
            onClick = viewModel::login
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.auth_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToRegister) {
                Text(text = stringResource(R.string.auth_register), fontWeight = FontWeight.Bold)
            }
        }
    }

    state.forgotPassword?.let { dialog ->
        ForgotPasswordDialog(
            state = dialog,
            onEmailChange = viewModel::onForgotEmailChange,
            onSend = viewModel::sendResetLink,
            onDismiss = viewModel::dismissForgotPassword
        )
    }
}

/**
 * "Forgot password" dialog. The prototype has no email service, so the server answers with the
 * same neutral message whether or not the account exists.
 */
@Composable
private fun ForgotPasswordDialog(
    state: ForgotPasswordState,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.forgot_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.sent) {
                    Text(stringResource(R.string.forgot_done))
                } else {
                    Text(stringResource(R.string.forgot_body))
                    LootTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = stringResource(R.string.auth_email),
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                        errorMessage = state.emailError?.let { stringResource(it.messageRes()) }
                    )
                    state.error?.let { ErrorMessage(text = stringResource(it.messageRes())) }
                }
            }
        },
        confirmButton = {
            if (!state.sent) {
                TextButton(onClick = onSend, enabled = !state.isSending) {
                    Text(stringResource(R.string.forgot_send))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(if (state.sent) R.string.common_close else R.string.common_cancel))
            }
        }
    )
}