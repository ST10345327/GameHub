package com.gamehub.app.data.remote.dto

/*
 * DTOs mirror the IGDB-proxy JSON from backend/README.md. Fields are nullable with defaults
 * since Gson fills missing fields with null; the repository checks them before building a model.
 */

data class GameDto(
    val id: Int? = null,
    val name: String? = null,
    val coverUrl: String? = null,
    val rating: Double? = null,
    val releaseYear: Int? = null,
    val releaseDate: String? = null,
    val genres: List<String>? = null,
    val platforms: List<String>? = null
)

data class SearchResponseDto(val items: List<GameDto>? = null, val hasMore: Boolean? = null)

data class SimilarGameDto(val id: Int? = null, val name: String? = null, val coverUrl: String? = null)

data class GameDetailsDto(
    val id: Int? = null,
    val name: String? = null,
    val coverUrl: String? = null,
    val rating: Double? = null,
    val releaseYear: Int? = null,
    val releaseDate: String? = null,
    val genres: List<String>? = null,
    val platforms: List<String>? = null,
    val description: String? = null,
    val developers: List<String>? = null,
    val publishers: List<String>? = null,
    val screenshots: List<String>? = null,
    val similarGames: List<SimilarGameDto>? = null
)

data class GameStatusDto(
    val libraryStatus: String? = null,
    val isFavourite: Boolean? = null,
    val isInWishlist: Boolean? = null
)