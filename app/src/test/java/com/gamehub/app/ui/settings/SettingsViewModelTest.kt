package com.gamehub.app.ui.settings

import com.gamehub.app.data.model.AppLanguage
import com.gamehub.app.data.model.ThemeMode
import com.gamehub.app.testing.FakeSettingsRepository
import com.gamehub.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()

    private fun createViewModel() = SettingsViewModel(repository)

    @Test
    fun setThemeMode_updatesState() = runTest {
        val viewModel = createViewModel()

        viewModel.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun setLanguage_updatesState() = runTest {
        val viewModel = createViewModel()

        viewModel.setLanguage(AppLanguage.SETSWANA)

        assertEquals(AppLanguage.SETSWANA, viewModel.uiState.value.language)
    }
}
