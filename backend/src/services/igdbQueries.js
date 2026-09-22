'use strict';

const { GENRE_FILTERS, PLATFORM_FILTERS } = require('./igdbFilters');
const { Errors } = require('../utils/errors');

/**
 * Builders for IGDB "Apicalypse" query bodies (plain text, e.g. `fields name; where id = 5;`).
 * Everything here is a pure function so it is easy to unit test without calling IGDB.
 */

// Fields needed for a card/list row.
const SUMMARY_FIELDS =
  'name, first_release_date, total_rating, total_rating_count, cover.image_id, genres.name, platforms.name';

// Extra fields for the details screen.
const DETAIL_FIELDS = `${SUMMARY_FIELDS}, summary, involved_companies.developer, involved_companies.publisher, involved_companies.company.name, screenshots.image_id, similar_games.name, similar_games.cover.image_id`;

const DAY_SECONDS = 24 * 60 * 60;

/** Escapes text placed inside an Apicalypse string literal. */
function escapeText(text) {
  return String(text).replace(/\\/g, '\\\\').replace(/"/g, '\\"');
}

const yearStart = (year) => Date.UTC(year, 0, 1) / 1000;

/** Builds the list of `where` conditions shared by search, browse and random. */
function buildConditions({ genre, platform, minRating, releasedAfter, releasedBefore }) {
  const conditions = ['version_parent = null']; // hide editions/bundles that duplicate a base game
  if (genre) conditions.push(GENRE_FILTERS[genre]);
  if (platform) conditions.push(PLATFORM_FILTERS[platform]);
  if (minRating) conditions.push(`total_rating >= ${Number(minRating) * 10}`);
  if (releasedAfter) conditions.push(`first_release_date >= ${yearStart(releasedAfter)}`);
  if (releasedBefore) conditions.push(`first_release_date < ${yearStart(releasedBefore)}`);
  return conditions;
}

/**
 * Query for GET /games/search.
 * IGDB result ordering is used only when there is no text query; with a text query IGDB ranks
 * by relevance and the service re-sorts the page itself (see gameService.sortGames).
 */
function buildSearchQuery(params) {
  const { q, sort = 'relevance', limit = 20, offset = 0 } = params;
  const conditions = buildConditions(params);
  const parts = [`fields ${SUMMARY_FIELDS};`];

  if (q) parts.push(`search "${escapeText(q)}";`);

  if (!q) {
    // Browsing (genre/platform only): use IGDB ordering.
    if (sort === 'rating') {
      conditions.push('total_rating != null');
      parts.push('sort total_rating desc;');
    } else if (sort === 'release') {
      conditions.push('first_release_date != null');
      parts.push('sort first_release_date desc;');
    } else if (sort === 'title') {
      parts.push('sort name asc;');
    } else {
      conditions.push('cover != null');
      parts.push('sort total_rating_count desc;');
    }
  }

  parts.push(`where ${conditions.join(' & ')};`);
  parts.push(`limit ${limit};`);
  parts.push(`offset ${offset};`);
  return parts.join(' ');
}

/** Query for GET /games/:id */
function buildDetailsQuery(id) {
  if (!Number.isInteger(id) || id < 1) throw Errors.validation({ gameId: 'Must be a valid game id.' });
  return `fields ${DETAIL_FIELDS}; where id = ${id};`;
}

/**
 * Queries for the Home rows. `nowSeconds` is passed in (not read from the clock) so the
 * function stays pure and testable.
 */
function buildDiscoverQuery(category, nowSeconds, limit = 12) {
  const base = ['version_parent = null', 'cover != null'];
  switch (category) {
    case 'trending': // well-known games released in the last six months
      return `fields ${SUMMARY_FIELDS}; where ${[...base, `first_release_date > ${nowSeconds - 180 * DAY_SECONDS}`, `first_release_date <= ${nowSeconds}`, 'total_rating_count > 5'].join(' & ')}; sort total_rating_count desc; limit ${limit};`;
    case 'popular': // most rated of all time
      return `fields ${SUMMARY_FIELDS}; where ${[...base, 'total_rating_count > 100'].join(' & ')}; sort total_rating_count desc; limit ${limit};`;
    case 'upcoming': // not released yet, soonest first
      return `fields ${SUMMARY_FIELDS}; where ${[...base, `first_release_date > ${nowSeconds}`].join(' & ')}; sort first_release_date asc; limit ${limit};`;
    case 'top-rated': // best rated among games with enough votes to be meaningful
      return `fields ${SUMMARY_FIELDS}; where ${[...base, 'total_rating_count > 200', 'total_rating != null'].join(' & ')}; sort total_rating desc; limit ${limit};`;
    default:
      throw Errors.validation({ category: 'Unknown discovery category.' });
  }
}

/** Conditions used for "Surprise Me": well-known games only, so results are worth suggesting. */
function buildRandomConditions(params) {
  return [...buildConditions(params), 'cover != null', 'total_rating_count > 10'];
}

const buildRandomCountQuery = (params) => `where ${buildRandomConditions(params).join(' & ')};`;

const buildRandomPickQuery = (params, offset) =>
  `fields ${SUMMARY_FIELDS}; where ${buildRandomConditions(params).join(' & ')}; sort id asc; limit 1; offset ${offset};`;

module.exports = {
  escapeText,
  buildSearchQuery,
  buildDetailsQuery,
  buildDiscoverQuery,
  buildRandomCountQuery,
  buildRandomPickQuery,
};