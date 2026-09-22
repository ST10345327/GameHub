package com.gamehub.app.domain

import com.gamehub.app.data.model.Game
import com.gamehub.app.data.model.GameDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameComparisonTest {

    private fun details(
        id: Int = 1, rating: Double? = 8.0, genres: List<String> = listOf("RPG"),
        platforms: List<String> = listOf("PC"), developers: List<String> = listOf("Studio"),
        publishers: List<String> = emptyList(), releaseDate: String? = "2020-05-01"
    ) = GameDetails(
        game = Game(id, "Game $id", null, rating, releaseDate?.take(4)?.toInt(), releaseDate, genres, platforms),
        description = null, developers = developers, publishers = publishers, screenshots = emptyList(), similarGames = emptyList()
    )

    @Test
    fun higherRating_wins() {
        assertEquals(Winner.FIRST, GameComparison.ratingWinner(9.5, 8.0))
        assertEquals(Winner.SECOND, GameComparison.ratingWinner(7.0, 8.1))
    }

    @Test
    fun equalRatings_areATie_andMissingRatingsCannotBeCompared() {
        assertEquals(Winner.TIE, GameComparison.ratingWinner(8.0, 8.0))
        assertEquals(Winner.NONE, GameComparison.ratingWinner(null, 8.0))
        assertEquals(Winner.NONE, GameComparison.ratingWinner(null, null))
    }

    @Test
    fun formatRating_usesOneDecimalWithADot() {
        assertEquals("9.5", GameComparison.formatRating(9.5))
        assertEquals("7.3", GameComparison.formatRating(7.26))
    }

    @Test
    fun compare_joinsListsAndUsesNullForMissingData() {
        val a = details(1, rating = 9.0, genres = listOf("RPG", "Action"), publishers = listOf("Pub"))
        val b = details(2, rating = null, genres = emptyList(), platforms = listOf("PC", "PS5"))
        val rows = GameComparison.compare(a, b).associateBy { it.row }

        assertEquals("9.0", rows.getValue(ComparisonRow.RATING).first)
        assertNull(rows.getValue(ComparisonRow.RATING).second)
        assertEquals("RPG, Action", rows.getValue(ComparisonRow.GENRE).first)
        assertEquals("PC, PS5", rows.getValue(ComparisonRow.PLATFORMS).second)
        assertEquals("Pub", rows.getValue(ComparisonRow.PUBLISHER).first)
        assertNull(rows.getValue(ComparisonRow.PUBLISHER).second)
    }
}