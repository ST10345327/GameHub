package com.gamehub.app.ui.compare

import com.gamehub.app.data.remote.ApiError
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.testing.FakeGameRepository
import com.gamehub.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompareViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGameRepository()

    private fun createViewModel(firstGameId: Int = 1) = CompareViewModel(firstGameId, repository)

    @Test
    fun initialMode_loadsFirstGame() = runTest {
        val details = FakeGameRepository.TEST_DETAILS
        repository.detailsResult = ApiResult.Success(details)

        val viewModel = createViewModel(details.game.id)

        assertFalse(viewModel.uiState.value.isLoadingFirst)
        assertEquals(details, viewModel.uiState.value.firstGame)
    }

    @Test
    fun selectSecondGame_loadsDetails() = runTest {
        val game2 = FakeGameRepository.TEST_GAME.copy(id = 2)
        val details2 = FakeGameRepository.TEST_DETAILS.copy(game = game2)
        repository.detailsResult = ApiResult.Success(details2)

        val viewModel = createViewModel()
        viewModel.selectSecondGame(game2)

        assertFalse(viewModel.uiState.value.isLoadingSecond)
        assertEquals(details2, viewModel.uiState.value.secondGame)
    }

    @Test
    fun pickerSearch_debounces_andFiltersFirstGame() = runTest {
        val firstId = 1
        val game1 = FakeGameRepository.TEST_GAME.copy(id = firstId)
        val game2 = FakeGameRepository.TEST_GAME.copy(id = 2)
        repository.searchResult = ApiResult.Success(listOf(game1, game2))

        val viewModel = createViewModel(firstId)
        viewModel.onPickerQueryChange("Test")

        advanceTimeBy(500)

        val state = viewModel.uiState.value
        assertFalse(state.isPickerLoading)
        assertEquals(listOf(game2), state.pickerResults)
    }

    @Test
    fun loadFailure_showsError() = runTest {
        repository.detailsResult = ApiResult.Failure(ApiError.NoConnection)

        val viewModel = createViewModel()

        assertEquals(ApiError.NoConnection, viewModel.uiState.value.error)
    }

    @Test
    fun clearSecondGame_resetsState() = runTest {
        val viewModel = createViewModel()
        viewModel.selectSecondGame(FakeGameRepository.TEST_GAME)

        viewModel.clearSecondGame()

        assertNull(viewModel.uiState.value.secondGame)
    }
}
