package com.gamehub.app.data.local

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gamehub.app.data.model.AppLanguage
import com.gamehub.app.data.model.AppSettings
import com.gamehub.app.data.model.ThemeMode
import com.gamehub.app.utils.AppLogger
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Persists appearance and language choices in the same DataStore file as [SessionStore], so a
 * signed-out user still keeps their preferred theme and language.
 */
class SettingsStore(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("app_language")
    }

    val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                AppLogger.error("Could not read saved settings", error)
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { prefs ->
            AppSettings(
                themeMode = ThemeMode.fromApi(prefs[Keys.THEME]),
                language = AppLanguage.fromTag(prefs[Keys.LANGUAGE])
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME] = mode.apiValue }
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[Keys.LANGUAGE] = language.tag }
    }
}