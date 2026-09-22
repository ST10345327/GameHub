package com.gamehub.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gamehub.app.data.model.User
import com.gamehub.app.utils.AppLogger
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** The app's single DataStore file. Declared once per process, as DataStore requires. */
val Context.gameHubDataStore: DataStore<Preferences> by preferencesDataStore(name = "gamehub_prefs")

/**
 * Remembers who is signed in (login token + basic user info) and whether the onboarding
 * has been seen. Backed by DataStore, so it survives app restarts.
 *
 * Prototype note: the token is stored unencrypted in the app's private storage. A release
 * version would encrypt it (e.g. Android Keystore).
 */
class SessionStore(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = intPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
    }

    /** DataStore data, but a corrupted/unreadable file is treated as "empty" instead of crashing. */
    private val safeData: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) {
            AppLogger.error("Could not read saved session", error)
            emit(emptyPreferences())
        } else {
            throw error
        }
    }

    /** The signed-in user, or null when signed out. Emits again whenever it changes. */
    val user: Flow<User?> = safeData.map { prefs ->
        val token = prefs[Keys.TOKEN]
        val id = prefs[Keys.USER_ID]
        if (token.isNullOrBlank() || id == null) {
            null
        } else {
            User(id = id, username = prefs[Keys.USERNAME].orEmpty(), email = prefs[Keys.EMAIL].orEmpty())
        }
    }

    val onboardingSeen: Flow<Boolean> = safeData.map { it[Keys.ONBOARDING_SEEN] ?: false }

    /** The saved login token, or null. Used by the network layer. */
    suspend fun token(): String? = safeData.first()[Keys.TOKEN]

    suspend fun save(token: String, user: User) {
        dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USER_ID] = user.id
            prefs[Keys.USERNAME] = user.username
            prefs[Keys.EMAIL] = user.email
        }
    }

    /** Signs out. The onboarding flag is kept: a returning user should not see the intro again. */
    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.EMAIL)
        }
    }

    suspend fun markOnboardingSeen() {
        dataStore.edit { it[Keys.ONBOARDING_SEEN] = true }
    }
}