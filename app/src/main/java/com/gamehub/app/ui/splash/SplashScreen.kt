package com.gamehub.app.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gamehub.app.R
import com.gamehub.app.utils.AppLogger
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 1200L

/**
 * Shows the app mark briefly while the saved session is read, then calls [onFinished] with the
 * route to open first (Home, Login or Onboarding).
 */
@Composable
fun SplashScreen(viewModel: SplashViewModel, onFinished: (startRoute: String) -> Unit) {
    // Always call the latest lambda, even if the composable recomposes during the delay.
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        AppLogger.debug("Splash shown")
        val route = viewModel.resolveStartRoute()
        delay(SPLASH_DURATION_MS)
        AppLogger.debug("Splash finished, starting at $route")
        currentOnFinished(route)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.loot_gamehub),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}