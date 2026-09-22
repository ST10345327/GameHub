package com.gamehub.app.ui.surprise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamehub.app.data.model.Game
import com.gamehub.app.data.model.GenreFilter
import com.gamehub.app.data.model.PlatformFilter
import com.gamehub.app.data.model.SearchFilters
import com.gamehub.app.data.remote.ApiError
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.data.repository.GameRepository
import com.gamehub.app.utils.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SurpriseMeUiState(
    val game: Game? = null,
    val genre: GenreFilter? = null,
    val platform: PlatformFilter? = null,
    val isLoading: Boolean = true,
    val error: ApiError? = null
)

/** Picks one random game matching the optional genre/platform filters (Part 1, section 4.9). */
class SurpriseMeViewModel(private val gameRepository: GameRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SurpriseMeUiState())
    val uiState: StateFlow<SurpriseMeUiState> = _uiState.asStateFlow()

    init { roll() }

    fun onGenreSelected(genre: GenreFilter?) { _uiState.update { it.copy(genre = genre) }; roll() }
    fun onPlatformSelected(platform: PlatformFilter?) { _uiState.update { it.copy(platform = platform) }; roll() }

    fun roll() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val filters = SearchFilters(genre = _uiState.value.genre, platform = _uiState.value.platform)
        viewModelScope.launch {
            when (val result = gameRepository.randomGame(filters)) {
                is ApiResult.Success -> _uiState.update { it.copy(game = result.data, isLoading = false) }
                is ApiResult.Failure -> {
                    AppLogger.warn("Surprise Me failed: ${result.error}")
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
}