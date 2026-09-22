'use strict';

/**
 * Maps the friendly filter names used by the Android app to IGDB "where" fragments, so the
 * app never needs to know IGDB's numeric ids. IGDB ids are documented at
 * https://api-docs.igdb.com/ (genres, themes, platforms endpoints).
 *
 * NOTE: `roguelite` filters on keyword names. It could not be verified against live IGDB
 * in development, so check it once with real credentials.
 */
const GENRE_FILTERS = Object.freeze({
  rpg: 'genres = (12)',
  adventure: 'genres = (31)',
  strategy: 'genres = (15,11,16)', // Strategy, Real Time Strategy, Turn-based strategy
  indie: 'genres = (32)',
  shooter: 'genres = (5)',
  puzzle: 'genres = (9)',
  platformer: 'genres = (8)',
  racing: 'genres = (10)',
  sport: 'genres = (14)',
  fighting: 'genres = (4)',
  simulator: 'genres = (13)',
  // "Action" and "Sandbox" are IGDB themes rather than genres.
  action: 'themes = (1)',
  sandbox: 'themes = (33)',
  roguelite: 'keywords.name = ("roguelite","roguelike")',
});

const PLATFORM_FILTERS = Object.freeze({
  pc: 'platforms = (6)',
  ps5: 'platforms = (167)',
  ps4: 'platforms = (48)',
  xboxseries: 'platforms = (169)',
  xboxone: 'platforms = (49)',
  switch: 'platforms = (130)',
  android: 'platforms = (34)',
  ios: 'platforms = (39)',
});

const SORT_OPTIONS = ['relevance', 'rating', 'release', 'title'];
const DISCOVER_CATEGORIES = ['trending', 'popular', 'upcoming', 'top-rated'];

module.exports = { GENRE_FILTERS, PLATFORM_FILTERS, SORT_OPTIONS, DISCOVER_CATEGORIES };