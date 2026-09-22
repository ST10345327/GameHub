package com.gamehub.app.data.remote.dto

data class SavedGameDto(
    val gameId: Int? = null,
    val name: String? = null,
    val coverUrl: String? = null,
    val rating: Double? = null,
    val releaseYear: Int? = null,
    val genre: String? = null,
    val platforms: String? = null,
    val status: String? = null,
    val addedAt: String? = null
)

data class SavedGameListDto(val items: List<SavedGameDto>? = null)

data class SavedGameRequest(
    val name: String,
    val coverUrl: String?,
    val rating: Double?,
    val releaseYear: Int?,
    val genre: String?,
    val platforms: String?,
    val status: String? = null
)

data class MoveToLibraryRequest(val status: String? = null)