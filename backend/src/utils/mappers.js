'use strict';

/** Converts a database `users` row to the public user JSON. Never includes the password hash. */
function toUserDto(row) {
  return {
    id: row.user_id,
    username: row.username,
    email: row.email,
    preferredLanguage: row.preferred_language,
    themePreference: row.theme_preference,
    notificationsEnabled: Boolean(row.notifications_enabled),
    wishlistAlerts: Boolean(row.wishlist_alerts),
    releaseAlerts: Boolean(row.release_alerts),
    createdAt: row.created_at,
  };
}

/** Converts a saved-game row (library / favourites / wishlist / recently_viewed) to JSON. */
function toSavedGameDto(row) {
  return {
    gameId: row.game_id,
    name: row.game_name,
    coverUrl: row.cover_url,
    rating: row.rating,
    releaseYear: row.release_year,
    genre: row.genre,
    platforms: row.platforms,
    status: row.status, // undefined (omitted from JSON) for tables without a status column
    addedAt: row.added_at ?? row.viewed_at,
  };
}

module.exports = { toUserDto, toSavedGameDto };