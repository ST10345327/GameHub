'use strict';

const express = require('express');
const { asyncHandler, Errors } = require('../utils/errors');
const { toUserDto } = require('../utils/mappers');
const { parseSettings, parseGameId } = require('../utils/validation');

/**
 * Endpoints about the signed-in user (all require a token):
 *   GET   /me                        profile and settings
 *   PATCH /me/settings               language, theme and notification preferences
 *   GET   /me/games/:gameId/status   is this game in my library / favourites / wishlist?
 */
function createMeRouter({ pool }) {
  const router = express.Router();

  router.get(
    '/',
    asyncHandler(async (req, res) => {
      const [rows] = await pool.execute('SELECT * FROM users WHERE user_id = ?', [req.userId]);
      if (!rows[0]) throw Errors.unauthorized();
      res.json({ user: toUserDto(rows[0]) });
    })
  );

  router.patch(
    '/settings',
    asyncHandler(async (req, res) => {
      const update = parseSettings(req.body);
      // Column names come from our own whitelist in parseSettings, never from user input,
      // so building the SET clause dynamically is safe. Values are still bound parameters.
      const columns = Object.keys(update);
      const setClause = columns.map((c) => `${c} = ?`).join(', ');
      await pool.execute(`UPDATE users SET ${setClause} WHERE user_id = ?`, [
        ...columns.map((c) => update[c]),
        req.userId,
      ]);
      const [rows] = await pool.execute('SELECT * FROM users WHERE user_id = ?', [req.userId]);
      res.json({ user: toUserDto(rows[0]) });
    })
  );

  router.get(
    '/games/:gameId/status',
    asyncHandler(async (req, res) => {
      const gameId = parseGameId(req.params.gameId);
      const [[library], [favourite], [wishlist]] = await Promise.all([
        pool.execute('SELECT status FROM library WHERE user_id = ? AND game_id = ?', [req.userId, gameId]),
        pool.execute('SELECT 1 FROM favourites WHERE user_id = ? AND game_id = ?', [req.userId, gameId]),
        pool.execute('SELECT 1 FROM wishlist WHERE user_id = ? AND game_id = ?', [req.userId, gameId]),
      ]);
      res.json({
        libraryStatus: library[0] ? library[0].status : null,
        isFavourite: favourite.length > 0,
        isInWishlist: wishlist.length > 0,
      });
    })
  );

  return router;
}

module.exports = { createMeRouter };