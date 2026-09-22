package com.gamehub.app.data.repository

import com.gamehub.app.data.model.Game
import com.gamehub.app.data.model.LibraryStatus
import com.gamehub.app.data.model.SavedGame
import com.gamehub.app.data.remote.ApiCaller
import com.gamehub.app.data.remote.ApiResult
import com.gamehub.app.data.remote.GameHubApi
import com.gamehub.app.data.remote.dto.MoveToLibraryRequest
import com.gamehub.app.data.remote.dto.SavedGameDto
import com.gamehub.app.data.remote.dto.SavedGameListDto
import com.gamehub.app.data.remote.dto.SavedGameRequest

/** Everything about the user's own game lists: library (with status), favourites and wishlist. */
interface LibraryRepository {
    suspend fun getLibrary(status: LibraryStatus? = null): ApiResult<List<SavedGame>>
    suspend fun saveToLibrary(game: Game, status: LibraryStatus): ApiResult<SavedGame>
    suspend fun removeFromLibrary(gameId: Int): ApiResult<Unit>

    suspend fun getFavourites(): ApiResult<List<SavedGame>>
    suspend fun addFavourite(game: Game): ApiResult<SavedGame>
    suspend fun removeFavourite(gameId: Int): ApiResult<Unit>

    suspend fun getWishlist(): ApiResult<List<SavedGame>>
    suspend fun addToWishlist(game: Game): ApiResult<SavedGame>
    suspend fun removeFromWishlist(gameId: Int): ApiResult<Unit>
    suspend fun moveToLibrary(gameId: Int, status: LibraryStatus): ApiResult<SavedGame>
}

class LibraryRepositoryImpl(
    private val api: GameHubApi,
    private val apiCaller: ApiCaller
) : LibraryRepository {

    override suspend fun getLibrary(status: LibraryStatus?): ApiResult<List<SavedGame>> =
        apiCaller.call { api.getLibrary(status?.apiValue).toSavedGames() }

    override suspend fun saveToLibrary(game: Game, status: LibraryStatus): ApiResult<SavedGame> =
        apiCaller.call { api.putLibraryGame(game.id, game.toRequest(status)).toSavedGame() }

    override suspend fun removeFromLibrary(gameId: Int): ApiResult<Unit> =
        apiCaller.call { api.deleteLibraryGame(gameId) }

    override suspend fun getFavourites(): ApiResult<List<SavedGame>> =
        apiCaller.call { api.getFavourites().toSavedGames() }

    override suspend fun addFavourite(game: Game): ApiResult<SavedGame> =
        apiCaller.call { api.putFavourite(game.id, game.toRequest()).toSavedGame() }

    override suspend fun removeFavourite(gameId: Int): ApiResult<Unit> =
        apiCaller.call { api.deleteFavourite(gameId) }

    override suspend fun getWishlist(): ApiResult<List<SavedGame>> =
        apiCaller.call { api.getWishlist().toSavedGames() }

    override suspend fun addToWishlist(game: Game): ApiResult<SavedGame> =
        apiCaller.call { api.putWishlistGame(game.id, game.toRequest()).toSavedGame() }

    override suspend fun removeFromWishlist(gameId: Int): ApiResult<Unit> =
        apiCaller.call { api.deleteWishlistGame(gameId) }

    override suspend fun moveToLibrary(gameId: Int, status: LibraryStatus): ApiResult<SavedGame> =
        apiCaller.call { api.moveWishlistToLibrary(gameId, MoveToLibraryRequest(status.apiValue)).toSavedGame() }
}

/** The 255-character platforms column can't hold every platform, so only the first few are kept. */
private fun Game.toRequest(status: LibraryStatus? = null) = SavedGameRequest(
    name = name,
    coverUrl = coverUrl,
    rating = rating,
    releaseYear = releaseYear,
    genre = genres.firstOrNull(),
    platforms = platforms.take(4).joinToString(", ").take(255).ifBlank { null },
    status = status?.apiValue
)

private fun SavedGameListDto.toSavedGames(): List<SavedGame> = (items ?: emptyList()).mapNotNull { it.toSavedGameOrNull() }

private fun SavedGameDto.toSavedGame(): SavedGame =
    toSavedGameOrNull() ?: error("Saved game response was missing required fields")

private fun SavedGameDto.toSavedGameOrNull(): SavedGame? {
    val id = gameId ?: return null
    val n = name ?: return null
    return SavedGame(id, n, coverUrl, rating, releaseYear, genre, platforms, LibraryStatus.fromApi(status))
}