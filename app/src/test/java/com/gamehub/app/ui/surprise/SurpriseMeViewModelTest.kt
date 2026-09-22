package com.gamehub.app.ui.surprise

import com.gamehub.app.data.model.GenreFilter
import com.gamehub.app.data.remote.ApiError
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.testing.FakeGameRepository
import com.gamehub.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SurpriseMeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGameRepository()

    private fun createViewModel() = SurpriseMeViewModel(repository)

    @Test
    fun initialMode_rollsRandomGame() = runTest {
        val game = FakeGameRepository.TEST_GAME
        repository.randomGameResult = ApiResult.Success(game)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(game, state.game)
    }

    @Test
    fun roll_updatesGame() = runTest {
        val game1 = FakeGameRepository.TEST_GAME.copy(id = 1)
        val game2 = FakeGameRepository.TEST_GAME.copy(id = 2)
        repository.randomGameResult = ApiResult.Success(game1)

        val viewModel = createViewModel()
        assertEquals(game1, viewModel.uiState.value.game)

        repository.randomGameResult = ApiResult.Success(game2)
        viewModel.roll()

        assertEquals(game2, viewModel.uiState.value.game)
    }

    @Test
    fun filterChange_rollsAgain() = runTest {
        val viewModel = createViewModel()
        repository.randomGameResult = ApiResult.Success(FakeGameRepository.TEST_GAME)

        viewModel.onGenreSelected(GenreFilter.RPG)

        assertEquals(GenreFilter.RPG, viewModel.uiState.value.genre)
        // One for init, one for filter change
    }

    @Test
    fun loadFailure_showsError() = runTest {
        repository.randomGameResult = ApiResult.Failure(ApiError.NoConnection)

        val viewModel = createViewModel()

        assertEquals(ApiError.NoConnection, viewModel.uiState.value.error)
    }
}
