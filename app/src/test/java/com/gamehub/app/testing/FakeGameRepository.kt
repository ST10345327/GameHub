package com.gamehub.app.testing

import com.gamehub.app.data.model.DiscoverCategory
import com.gamehub.app.data.model.Game
import com.gamehub.app.data.model.GameDetails
import com.gamehub.app.data.model.GameStatus
import com.gamehub.app.data.model.SearchFilters
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.data.repository.GameRepository

class FakeGameRepository : GameRepository {
    var searchResult: ApiResult<List<Game>> = ApiResult.Success(emptyList())
    var discoverResult: ApiResult<List<Game>> = ApiResult.Success(emptyList())
    var randomGameResult: ApiResult<Game> = ApiResult.Success(TEST_GAME)
    var detailsResult: ApiResult<GameDetails> = ApiResult.Success(TEST_DETAILS)
    var statusResult: ApiResult<GameStatus> = ApiResult.Success(GameStatus())

    val searchCalls = mutableListOf<Pair<String?, SearchFilters>>()

    override suspend fun search(q: String?, filters: SearchFilters, limit: Int, offset: Int): ApiResult<List<Game>> {
        searchCalls.add(q to filters)
        return searchResult
    }

    override suspend fun discover(category: DiscoverCategory): ApiResult<List<Game>> = discoverResult

    override suspend fun randomGame(filters: SearchFilters): ApiResult<Game> = randomGameResult

    override suspend fun details(gameId: Int): ApiResult<GameDetails> = detailsResult

    override suspend fun status(gameId: Int): ApiResult<GameStatus> = statusResult

    companion object {
        val TEST_GAME = Game(id = 1, name = "Test Game", coverUrl = null, rating = 8.5, releaseYear = 2024, releaseDate = "2024-01-01", genres = listOf("Action"), platforms = listOf("PC"))
        val TEST_DETAILS = GameDetails(game = TEST_GAME, description = "Description", developers = listOf("Dev"), publishers = listOf("Pub"), screenshots = emptyList(), similarGames = emptyList())
    }
}
