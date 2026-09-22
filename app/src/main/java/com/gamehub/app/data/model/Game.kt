package com.gamehub.app.data.model

data class Game(
    val id: Int,
    val name: String,
    val coverUrl: String?,
    val rating: Double?,
    val releaseYear: Int?,
    val releaseDate: String?,
    val genres: List<String>,
    val platforms: List<String>
)

data class SimilarGame(val id: Int, val name: String, val coverUrl: String?)

data class GameDetails(
    val game: Game,
    val description: String?,
    val developers: List<String>,
    val publishers: List<String>,
    val screenshots: List<String>,
    val similarGames: List<SimilarGame>
)

/** Home screen discovery rows. */
enum class DiscoverCategory(val apiValue: String) {
    TRENDING("trending"), POPULAR("popular"), UPCOMING("upcoming"), TOP_RATED("top-rated")
}

/** Genre chips from Part 1, section 4.4. `slug` is sent to the API, which maps it to IGDB ids. */
enum class GenreFilter(val slug: String, val displayName: String) {
    RPG("rpg", "RPG"),
    ACTION("action", "Action"),
    SANDBOX("sandbox", "Sandbox"),
    ROGUELITE("roguelite", "Roguelite"),
    ADVENTURE("adventure", "Adventure"),
    STRATEGY("strategy", "Strategy"),
    INDIE("indie", "Indie")
}

enum class PlatformFilter(val slug: String, val displayName: String) {
    PC("pc", "PC"),
    PS5("ps5", "PlayStation 5"),
    PS4("ps4", "PlayStation 4"),
    XBOX_SERIES("xboxseries", "Xbox Series X|S"),
    XBOX_ONE("xboxone", "Xbox One"),
    SWITCH("switch", "Nintendo Switch"),
    ANDROID("android", "Android"),
    IOS("ios", "iOS")
}

enum class SortOption(val apiValue: String) {
    RELEVANCE("relevance"), RATING("rating"), RELEASE_DATE("release"), TITLE("title")
}

enum class ReleasePeriod { ANY, LAST_YEAR, LAST_FIVE_YEARS, CLASSIC }

/** Search filters from Part 1, section 4.4 (genre, platform, rating, release period, sorting). */
data class SearchFilters(
    val genre: GenreFilter? = null,
    val platform: PlatformFilter? = null,
    val minRating: Int = 0,
    val period: ReleasePeriod = ReleasePeriod.ANY,
    val sort: SortOption = SortOption.RELEVANCE
) {
    /** How many filters differ from the defaults; shown as a badge on the filter button. */
    val activeCount: Int
        get() = listOf(
            genre != null, platform != null, minRating > 0,
            period != ReleasePeriod.ANY, sort != SortOption.RELEVANCE
        ).count { it }

    fun releasedAfter(currentYear: Int): Int? = when (period) {
        ReleasePeriod.LAST_YEAR -> currentYear - 1
        ReleasePeriod.LAST_FIVE_YEARS -> currentYear - 5
        else -> null
    }

    fun releasedBefore(): Int? = if (period == ReleasePeriod.CLASSIC) CLASSIC_BEFORE_YEAR else null

    companion object {
        const val CLASSIC_BEFORE_YEAR = 2010
    }
}

enum class LibraryStatus(val apiValue: String) {
    WANT_TO_PLAY("want_to_play"),
    PLAYING("playing"),
    COMPLETED("completed");

    companion object {
        fun fromApi(value: String?): LibraryStatus? = entries.find { it.apiValue == value }
    }
}

data class GameStatus(
    val libraryStatus: LibraryStatus? = null,
    val isFavourite: Boolean = false,
    val isInWishlist: Boolean = false
)

data class SavedGame(
    val gameId: Int,
    val name: String,
    val coverUrl: String?,
    val rating: Double?,
    val releaseYear: Int?,
    val genre: String?,
    val platforms: String?,
    val status: LibraryStatus?
)