package com.gamehub.app.data.repository

import com.gamehub.app.data.model.DiscoverCategory
import com.gamehub.app.data.model.Game
import com.gamehub.app.data.model.GameDetails
import com.gamehub.app.data.model.GameStatus
import com.gamehub.app.data.model.LibraryStatus
import com.gamehub.app.data.model.SearchFilters
import com.gamehub.app.data.model.SimilarGame
import com.gamehub.app.data.remote.ApiCaller
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.data.remote.LootApi
import com.gamehub.app.data.remote.dto.GameDetailsDto
import com.gamehub.app.data.remote.dto.GameDto
import com.gamehub.app.data.remote.dto.SearchResponseDto

/** Everything about IGDB game data, reached through our own backend's proxy. */
interface GameRepository {
    suspend fun search(q: String?, filters: SearchFilters, limit: Int = 20, offset: Int = 0): ApiResult<List<Game>>
    suspend fun discover(category: DiscoverCategory): ApiResult<List<Game>>
    suspend fun randomGame(filters: SearchFilters): ApiResult<Game>
    suspend fun details(gameId: Int): ApiResult<GameDetails>
    suspend fun status(gameId: Int): ApiResult<GameStatus>
}

class GameRepositoryImpl(
    private val api: LootApi,
    private val apiCaller: ApiCaller,
    private val currentYear: Int
) : GameRepository {

    override suspend fun search(q: String?, filters: SearchFilters, limit: Int, offset: Int): ApiResult<List<Game>> =
        apiCaller.call {
            api.searchGames(
                q = q?.takeIf { it.isNotBlank() },
                genre = filters.genre?.slug,
                platform = filters.platform?.slug,
                minRating = filters.minRating.takeIf { it > 0 },
                releasedAfter = filters.releasedAfter(currentYear),
                releasedBefore = filters.releasedBefore(),
                sort = filters.sort.apiValue,
                limit = limit,
                offset = offset
            ).toGameList()
        }

    override suspend fun discover(category: DiscoverCategory): ApiResult<List<Game>> =
        apiCaller.call { api.discoverGames(category.apiValue).toGameList() }

    override suspend fun randomGame(filters: SearchFilters): ApiResult<Game> =
        apiCaller.call {
            api.randomGame(
                genre = filters.genre?.slug,
                platform = filters.platform?.slug,
                minRating = filters.minRating.takeIf { it > 0 }
            ).toGame()
        }

    override suspend fun details(gameId: Int): ApiResult<GameDetails> =
        apiCaller.call { api.gameDetails(gameId).toGameDetails() }

    // The transform runs inside apiCaller.call so a malformed DTO becomes a normal
    // ApiResult.Failure(Unknown) instead of an uncaught exception.
    override suspend fun status(gameId: Int): ApiResult<GameStatus> =
        apiCaller.call {
            val dto = api.gameStatus(gameId)
            GameStatus(
                libraryStatus = LibraryStatus.fromApi(dto.libraryStatus),
                isFavourite = dto.isFavourite ?: false,
                isInWishlist = dto.isInWishlist ?: false
            )
        }
}

private fun SearchResponseDto.toGameList(): List<Game> = (items ?: emptyList()).mapNotNull { it.toGameOrNull() }

private fun GameDto.toGameOrNull(): Game? {
    val safeId = id ?: return null
    val safeName = name ?: return null
    return Game(safeId, safeName, coverUrl, rating, releaseYear, releaseDate, genres ?: emptyList(), platforms ?: emptyList())
}

private fun GameDto.toGame(): Game = toGameOrNull() ?: error("Game response was missing id or name")

private fun GameDetailsDto.toGameDetails(): GameDetails {
    val game = GameDto(id, name, coverUrl, rating, releaseYear, releaseDate, genres, platforms).toGame()
    return GameDetails(
        game = game,
        description = description,
        developers = developers ?: emptyList(),
        publishers = publishers ?: emptyList(),
        screenshots = screenshots ?: emptyList(),
        similarGames = (similarGames ?: emptyList()).mapNotNull { s ->
            val sid = s.id ?: return@mapNotNull null
            val sname = s.name ?: return@mapNotNull null
            SimilarGame(sid, sname, s.coverUrl)
        }
    )
}
