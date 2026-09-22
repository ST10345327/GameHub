'use strict';

const express = require('express');
const { asyncHandler } = require('../utils/errors');
const { toSavedGameDto } = require('../utils/mappers');
const { parseGameId, parseSavedGame } = require('../utils/validation');

const MAX_RECENT = 20;

/**
 * Recently viewed games:
 *   GET    /recent           newest first (max 20)
 *   POST   /recent/:gameId   record a view (moves the game to the top); 204
 *   DELETE /recent           clear the history; 204
 */
function createRecentRouter({ pool }) {
  const router = express.Router();

  router.get(
    '/',
    asyncHandler(async (req, res) => {
      const [rows] = await pool.execute(
        `SELECT * FROM recently_viewed WHERE user_id = ? ORDER BY viewed_at DESC, view_id DESC LIMIT ${MAX_RECENT}`,
        [req.userId]
      );
      res.json({ items: rows.map(toSavedGameDto) });
    })
  );

  router.post(
    '/:gameId',
    asyncHandler(async (req, res) => {
      const gameId = parseGameId(req.params.gameId);
      const game = parseSavedGame(req.body);
      const values = [game.name, game.coverUrl, game.rating, game.releaseYear, game.genre, game.platforms];

      await pool.execute(
        `INSERT INTO recently_viewed
           (user_id, game_id, game_name, cover_url, rating, release_year, genre, platforms, viewed_at)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, UTC_TIMESTAMP(3))
         ON DUPLICATE KEY UPDATE
           game_name = ?, cover_url = ?, rating = ?, release_year = ?, genre = ?, platforms = ?,
           viewed_at = UTC_TIMESTAMP(3)`,
        [req.userId, gameId, ...values, ...values]
      );

      // Keep only the newest MAX_RECENT rows. The extra SELECT wrapper is required because
      // MySQL does not allow LIMIT directly inside an IN (...) subquery.
      await pool.execute(
        `DELETE FROM recently_viewed
         WHERE user_id = ? AND view_id NOT IN (
           SELECT view_id FROM (
             SELECT view_id FROM recently_viewed WHERE user_id = ?
             ORDER BY viewed_at DESC, view_id DESC LIMIT ${MAX_RECENT}
           ) AS newest
         )`,
        [req.userId, req.userId]
      );
      res.status(204).end();
    })
  );

  router.delete(
    '/',
    asyncHandler(async (req, res) => {
      await pool.execute('DELETE FROM recently_viewed WHERE user_id = ?', [req.userId]);
      res.status(204).end();
    })
  );

  return router;
}

module.exports = { createRecentRouter };