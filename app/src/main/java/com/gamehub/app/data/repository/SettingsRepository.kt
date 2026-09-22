package com.gamehub.app.data.repository

import com.gamehub.app.data.local.SettingsStore
import com.gamehub.app.data.model.AppLanguage
import com.gamehub.app.data.model.AppSettings
import com.gamehub.app.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: AppLanguage)
}

class SettingsRepositoryImpl(private val store: SettingsStore) : SettingsRepository {
    override val settings: Flow<AppSettings> = store.settings
    override suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)
    override suspend fun setLanguage(language: AppLanguage) = store.setLanguage(language)
}