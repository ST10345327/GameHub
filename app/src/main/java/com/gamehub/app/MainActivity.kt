package com.gamehub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gamehub.app.ui.navigation.GameHubNavHost
import com.gamehub.app.ui.theme.GameHubTheme
import com.gamehub.app.utils.AppLogger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLogger.debug("MainActivity created")

        val container = (application as GameHubApplication).container
        setContent {
            GameHubTheme {
                GameHubNavHost(container = container)
            }
        }
    }
}
