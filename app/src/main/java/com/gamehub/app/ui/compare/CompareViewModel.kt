package com.gamehub.app.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gamehub.app.data.model.Game
import com.gamehub.app.data.model.GameDetails
import com.gamehub.app.data.model.SearchFilters
import com.gamehub.app.data.remote.ApiError
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.data.repository.GameRepository
import com.gamehub.app.utils.AppLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompareUiState(
    val firstGame: GameDetails? = null,
    val secondGame: GameDetails? = null,
    val isLoadingFirst: Boolean = true,
    val isLoadingSecond: Boolean = false,
    val isPickerOpen: Boolean = false,
    val pickerQuery: String = "",
    val pickerResults: List<Game> = emptyList(),
    val isPickerLoading: Boolean = false,
    val error: ApiError? = null
)

private const val PICKER_DEBOUNCE_MS = 400L

/** Game A comes preselected (from the Game Details "Compare" action); Game B is picked here. */
class CompareViewModel(
    private val firstGameId: Int,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    private var pickerJob: Job? = null

    init {
        viewModelScope.launch {
            when (val result = gameRepository.details(firstGameId)) {
                is ApiResult.Success -> _uiState.update { it.copy(firstGame = result.data, isLoadingFirst = false) }
                is ApiResult.Failure -> _uiState.update { it.copy(isLoadingFirst = false, error = result.error) }
            }
        }
    }

    fun openPicker() { _uiState.update { it.copy(isPickerOpen = true) } }
    fun closePicker() { _uiState.update { it.copy(isPickerOpen = false, pickerQuery = "", pickerResults = emptyList()) } }
    fun clearSecondGame() { _uiState.update { it.copy(secondGame = null) } }

    fun onPickerQueryChange(value: String) {
        _uiState.update { it.copy(pickerQuery = value) }
        pickerJob?.cancel()
        if (value.isBlank()) { _uiState.update { it.copy(pickerResults = emptyList(), isPickerLoading = false) }; return }
        pickerJob = viewModelScope.launch {
            delay(PICKER_DEBOUNCE_MS)
            _uiState.update { it.copy(isPickerLoading = true) }
            when (val result = gameRepository.search(value, SearchFilters(), limit = 15)) {
                is ApiResult.Success -> _uiState.update { it.copy(pickerResults = result.data.filter { g -> g.id != firstGameId }, isPickerLoading = false) }
                is ApiResult.Failure -> {
                    AppLogger.warn("Compare picker search failed: ${result.error}")
                    _uiState.update { it.copy(isPickerLoading = false) }
                }
            }
        }
    }

    fun selectSecondGame(game: Game) {
        closePicker()
        _uiState.update { it.copy(isLoadingSecond = true, secondGame = null) }
        viewModelScope.launch {
            when (val result = gameRepository.details(game.id)) {
                is ApiResult.Success -> _uiState.update { it.copy(secondGame = result.data, isLoadingSecond = false) }
                is ApiResult.Failure -> {
                    AppLogger.warn("Could not load second game: ${result.error}")
                    _uiState.update { it.copy(isLoadingSecond = false, error = result.error) }
                }
            }
        }
    }

    companion object {
        /** Built per-screen because it needs the first game's id from navigation. */
        fun factory(firstGameId: Int, gameRepository: GameRepository): ViewModelProvider.Factory =
            viewModelFactory { initializer { CompareViewModel(firstGameId, gameRepository) } }
    }
}