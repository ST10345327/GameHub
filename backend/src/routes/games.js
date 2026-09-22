'use strict';

const express = require('express');
const { asyncHandler, Errors } = require('../utils/errors');
const { parseGameId } = require('../utils/validation');
const { parseSearchParams, parseRandomParams } = require('../services/searchParams');
const { DISCOVER_CATEGORIES } = require('../services/igdbFilters');

/**
 * IGDB proxy (all routes require a token, which stops strangers using our IGDB quota):
 *   GET /games/search?q=&genre=&platform=&minRating=&releasedAfter=&releasedBefore=&sort=&limit=&offset=
 *   GET /games/discover/:category      trending | popular | upcoming | top-rated
 *   GET /games/random?genre=&platform=&minRating=
 *   GET /games/:id
 * Route order matters: the fixed paths must be declared before "/:id".
 */
function createGamesRouter({ gameService }) {
  const router = express.Router();

  router.get(
    '/search',
    asyncHandler(async (req, res) => {
      res.json(await gameService.search(parseSearchParams(req.query)));
    })
  );

  router.get(
    '/discover/:category',
    asyncHandler(async (req, res) => {
      if (!DISCOVER_CATEGORIES.includes(req.params.category)) {
        throw Errors.validation({ category: `Must be one of: ${DISCOVER_CATEGORIES.join(', ')}.` });
      }
      res.json(await gameService.discover(req.params.category));
    })
  );

  router.get(
    '/random',
    asyncHandler(async (req, res) => {
      res.json(await gameService.random(parseRandomParams(req.query)));
    })
  );

  router.get(
    '/:id',
    asyncHandler(async (req, res) => {
      res.json(await gameService.details(parseGameId(req.params.id)));
    })
  );

  return router;
}

module.exports = { createGamesRouter };