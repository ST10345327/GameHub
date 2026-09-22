package com.gamehub.app.domain

import com.gamehub.app.data.model.GameDetails
import java.util.Locale

/** Rows shown on the comparison screen (Part 1, section 4.8), in display order. */
enum class ComparisonRow { RATING, RELEASE_DATE, GENRE, PLATFORMS, DEVELOPER, PUBLISHER }

/** Which game "wins" a row. Only rating has a winner; other rows are informational. */
enum class Winner { FIRST, SECOND, TIE, NONE }

data class ComparisonResult(val row: ComparisonRow, val first: String?, val second: String?, val winner: Winner)

/** Pure comparison logic, kept out of the UI so it can be unit tested. */
object GameComparison {

    fun compare(first: GameDetails, second: GameDetails): List<ComparisonResult> = listOf(
        ComparisonResult(
            row = ComparisonRow.RATING,
            first = first.game.rating?.let(::formatRating),
            second = second.game.rating?.let(::formatRating),
            winner = ratingWinner(first.game.rating, second.game.rating)
        ),
        info(ComparisonRow.RELEASE_DATE, first.game.releaseDate, second.game.releaseDate),
        info(ComparisonRow.GENRE, first.game.genres.joinedOrNull(), second.game.genres.joinedOrNull()),
        info(ComparisonRow.PLATFORMS, first.game.platforms.joinedOrNull(), second.game.platforms.joinedOrNull()),
        info(ComparisonRow.DEVELOPER, first.developers.joinedOrNull(), second.developers.joinedOrNull()),
        info(ComparisonRow.PUBLISHER, first.publishers.joinedOrNull(), second.publishers.joinedOrNull())
    )

    fun ratingWinner(first: Double?, second: Double?): Winner = when {
        first == null || second == null -> Winner.NONE
        first > second -> Winner.FIRST
        second > first -> Winner.SECOND
        else -> Winner.TIE
    }

    fun formatRating(rating: Double): String = String.format(Locale.ROOT, "%.1f", rating)

    private fun info(row: ComparisonRow, first: String?, second: String?) = ComparisonResult(row, first, second, Winner.NONE)
    private fun List<String>.joinedOrNull(): String? = takeIf { it.isNotEmpty() }?.joinToString(", ")
}