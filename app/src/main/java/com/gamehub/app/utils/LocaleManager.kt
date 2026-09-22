package com.gamehub.app.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gamehub.app.data.model.AppLanguage

/**
 * Applies the app's UI language using AppCompat's per-app language API. This works without
 * restarting the Activity — every stringResource() call recomposes with the new language
 * automatically.
 */
object LocaleManager {
    fun applyLanguage(language: AppLanguage) {
        val locales = LocaleListCompat.forLanguageTags(language.tag)
        if (AppCompatDelegate.getApplicationLocales() != locales) {
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}