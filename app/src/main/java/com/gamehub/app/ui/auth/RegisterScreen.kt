package com.gamehub.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Registration screen (Part 1, section 4.2). Shows validation messages next to each field and
 * server problems above the button. The password is sent over the network only once, to the
 * API, which hashes it with bcrypt; the app never stores it.
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistered: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) onRegistered()
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
        Text(
            text = stringResource(R.string.auth_create_account_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(32.dp))

        LootTextField(
            value = state.username,
            onValueChange = viewModel::onUsernameChange,
            label = stringResource(R.string.auth_username),
            leadingIcon = Icons.Filled.Person,
            errorMessage = state.usernameError?.let { stringResource(it.messageRes()) }
        )
        Spacer(Modifier.height(12.dp))
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
            errorMessage = state.passwordError?.let { stringResource(it.messageRes()) }
        )
        // Password rule shown up front so users do not have to fail once to learn it.
        if (state.passwordError == null) {
            Text(
                text = stringResource(R.string.auth_password_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
                    .align(Alignment.Start)
            )
        }
        Spacer(Modifier.height(12.dp))
        LootTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = stringResource(R.string.auth_confirm_password),
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            errorMessage = state.confirmPasswordError?.let { stringResource(it.messageRes()) }
        )
        Spacer(Modifier.height(16.dp))

        state.error?.let { ErrorMessage(text = stringResource(it.messageRes())) }

        LoadingButton(
            text = stringResource(R.string.auth_create_account_button),
            isLoading = state.isLoading,
            onClick = viewModel::register
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.auth_have_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(text = stringResource(R.string.auth_sign_in), fontWeight = FontWeight.Bold)
            }
        }
    }
}