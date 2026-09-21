package com.gamehub.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gamehub.app.ui.navigation.GameHubNavHost
import com.gamehub.app.ui.theme.GameHubTheme

private const val TAG = "MainActivity"

/**
 * Single-activity app: every screen is a composable destination inside [GameHubNavHost].
 * Phase 5 passes the saved theme choice into [GameHubTheme] here.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "onCreate")

        setContent {
            GameHubTheme {
                GameHubNavHost()
            }
        }
    }
}