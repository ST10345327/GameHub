package com.gamehub.app.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gamehub.app.R
import com.gamehub.app.ui.components.NavRow

private const val TAG = "SettingsScreen"

// Version shown in the About section. Move to BuildConfig/versionName later if needed.
private const val APP_VERSION = "1.0.0"

/**
 * Settings skeleton with the six sections from Part 1, section 4.10.
 * Phase 5 turns Appearance (Light/Dark/System) and Language (English/Setswana/isiZulu)
 * into working controls that persist, and adds the remaining sections' behaviour.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val sections = listOf(
        R.string.settings_appearance,
        R.string.settings_language,
        R.string.settings_notifications,
        R.string.settings_data,
        R.string.settings_account,
        R.string.settings_about
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        sections.forEach { sectionRes ->
            NavRow(
                textRes = sectionRes,
                onClick = { Log.d(TAG, "Section tapped (wired up in Phase 5)") }
            )
        }

        Text(
            text = stringResource(R.string.settings_version, APP_VERSION),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}