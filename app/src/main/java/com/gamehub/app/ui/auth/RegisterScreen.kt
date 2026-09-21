package com.gamehub.app.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gamehub.app.R
import com.gamehub.app.ui.components.GameHubTextField

private const val TAG = "RegisterScreen"

/**
 * Registration form from Part 1, section 4.2.
 * Phase 2 adds input validation (8+ characters, contains a number, passwords match),
 * the REST API call, and server-side password hashing.
 */
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

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

        GameHubTextField(
            value = username,
            onValueChange = { username = it },
            label = stringResource(R.string.auth_username),
            leadingIcon = Icons.Filled.Person
        )
        Spacer(Modifier.height(12.dp))
        GameHubTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.auth_email),
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        GameHubTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.auth_password),
            leadingIcon = Icons.Filled.Lock,
            isPassword = true
        )
        Spacer(Modifier.height(12.dp))
        GameHubTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = stringResource(R.string.auth_confirm_password),
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            imeAction = ImeAction.Done
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                Log.d(TAG, "Create account tapped")
                onRegistered()
            },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.auth_create_account_button),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.auth_have_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = stringResource(R.string.auth_sign_in),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}