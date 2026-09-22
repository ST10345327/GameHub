'use strict';

const { Errors } = require('../utils/errors');
const { TtlCache } = require('../utils/ttlCache');
const { toGameSummary, toGameDetails } = require('./igdbMapper');
const {
  buildSearchQuery,
  buildDetailsQuery,
  buildDiscoverQuery,
  buildRandomCountQuery,
  buildRandomPickQuery,
} = require('./igdbQueries');

const RANDOM_WINDOW = 500; // pick from at most this many matches
const compareNullsLast = (a, b, direction) => {
  if (a == null && b == null) return 0;
  if (a == null) return 1;
  if (b == null) return -1;
  return a < b ? -direction : a > b ? direction : 0;
};

/**
 * Re-sorts one page of text-search results. IGDB ranks text searches by relevance and the
 * results are only one page long, so this orders within that page.
 */
function sortGames(games, sort) {
  const sorted = [...games];
  if (sort === 'rating') sorted.sort((a, b) => compareNullsLast(a.rating, b.rating, -1));
  else if (sort === 'release') sorted.sort((a, b) => compareNullsLast(a.releaseDate, b.releaseDate, -1));
  else if (sort === 'title') sorted.sort((a, b) => a.name.localeCompare(b.name));
  return sorted;
}

/**
 * Business logic for game data: builds queries, calls IGDB, maps results to our JSON and
 * caches the rows that never need to be fresh to the second.
 * All collaborators are injected, which is what makes this class unit testable.
 */
function createGameService({ igdb, now = () => Date.now(), random = Math.random }) {
  const discoverCache = new TtlCache(10 * 60 * 1000, now);
  const detailsCache = new TtlCache(5 * 60 * 1000, now);

  return {
    async search(params) {
      const raw = await igdb.query('games', buildSearchQuery(params));
      let items = raw.map(toGameSummary);
      if (params.q && params.sort && params.sort !== 'relevance') items = sortGames(items, params.sort);
      return { items, hasMore: raw.length === params.limit };
    },

    async details(id) {
      const cached = detailsCache.get(id);
      if (cached) return cached;

      const raw = await igdb.query('games', buildDetailsQuery(id));
      if (!raw.length) throw Errors.notFound('Game not found.');
      const details = toGameDetails(raw[0]);
      detailsCache.set(id, details);
      return details;
    },

    async discover(category) {
      const cached = discoverCache.get(category);
      if (cached) return cached;

      const nowSeconds = Math.floor(now() / 1000);
      const raw = await igdb.query('games', buildDiscoverQuery(category, nowSeconds));
      const result = { items: raw.map(toGameSummary), hasMore: false };
      discoverCache.set(category, result);
      return result;
    },

    /** Surprise Me: count the matches, then fetch one at a random position. */
    async random(params) {
      const { count } = await igdb.query('games/count', buildRandomCountQuery(params));
      if (!count) throw Errors.notFound('No games match those filters. Try loosening them.');

      const offset = Math.floor(random() * Math.min(count, RANDOM_WINDOW));
      const raw = await igdb.query('games', buildRandomPickQuery(params, offset));
      if (!raw.length) throw Errors.notFound('No games match those filters. Try loosening them.');
      return toGameSummary(raw[0]);
    },
  };
}

module.exports = { createGameService, sortGames };