package com.gamehub.app

import android.app.Application
import com.gamehub.app.di.AppContainer
import com.gamehub.app.utils.AppLogger

/**
 * Runs once when the app process starts. Its only job is to create the [AppContainer] that
 * holds the app's shared services. It must be named in AndroidManifest.xml (android:name).
 */
class LootApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        AppLogger.debug("Loot - Gamehub application started")
    }
}
