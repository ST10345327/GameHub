'use strict';

const express = require('express');
const { asyncHandler, Errors } = require('../utils/errors');
const { toSavedGameDto } = require('../utils/mappers');
const { parseGameId, parseSavedGame, parseOptionalStatus } = require('../utils/validation');

/** Columns that hold the snapshot of the game, identical in every saved-game table. */
const SNAPSHOT_COLUMNS = ['game_name', 'cover_url', 'rating', 'release_year', 'genre', 'platforms'];

/** Maps a validated request body to the values for SNAPSHOT_COLUMNS (same order). */
function snapshotValues(game) {
  return [game.name, game.coverUrl, game.rating, game.releaseYear, game.genre, game.platforms];
}

/**
 * Builds a router for one of the user's game lists. The same code serves the library,
 * favourites and wishlist, which differ only in table name and whether a status exists:
 *
 *   GET    /            list (library also accepts ?status=)
 *   PUT    /:gameId     add or update (idempotent "upsert")
 *   DELETE /:gameId     remove (204 even if it was not there)
 *
 * `table` is always a constant from our own code, never user input, so interpolating it
 * into the SQL text is safe. All values are bound parameters.
 */
function createSavedGamesRouter({ pool, table, hasStatus = false }) {
  const router = express.Router();

  router.get(
    '/',
    asyncHandler(async (req, res) => {
      const params = [req.userId];
      let sql = `SELECT * FROM ${table} WHERE user_id = ?`;

      if (hasStatus) {
        const status = parseOptionalStatus(req.query.status);
        if (status) {
          sql += ' AND status = ?';
          params.push(status);
        }
      }
      sql += ' ORDER BY added_at DESC, game_id DESC';

      const [rows] = await pool.execute(sql, params);
      res.json({ items: rows.map(toSavedGameDto) });
    })
  );

  router.put(
    '/:gameId',
    asyncHandler(async (req, res) => {
      const gameId = parseGameId(req.params.gameId);
      const game = parseSavedGame(req.body, { requireStatus: hasStatus });

      const columns = [...(hasStatus ? ['status'] : []), ...SNAPSHOT_COLUMNS];
      const values = [...(hasStatus ? [game.status] : []), ...snapshotValues(game)];

      // Bound twice (insert values, then update values) instead of using VALUES(),
      // which newer MySQL versions deprecate.
      await pool.execute(
        `INSERT INTO ${table} (user_id, game_id, ${columns.join(', ')})
         VALUES (?, ?, ${columns.map(() => '?').join(', ')})
         ON DUPLICATE KEY UPDATE ${columns.map((c) => `${c} = ?`).join(', ')}`,
        [req.userId, gameId, ...values, ...values]
      );

      const [rows] = await pool.execute(`SELECT * FROM ${table} WHERE user_id = ? AND game_id = ?`, [
        req.userId,
        gameId,
      ]);
      res.json(toSavedGameDto(rows[0]));
    })
  );

  router.delete(
    '/:gameId',
    asyncHandler(async (req, res) => {
      const gameId = parseGameId(req.params.gameId);
      await pool.execute(`DELETE FROM ${table} WHERE user_id = ? AND game_id = ?`, [req.userId, gameId]);
      res.status(204).end();
    })
  );

  return router;
}

/**
 * Wishlist = the generic list plus one extra action:
 *   POST /wishlist/:gameId/move-to-library   body { status? } (default want_to_play)
 * Copying to the library and deleting from the wishlist happen in one transaction so a
 * failure can never leave the game in both lists or in neither.
 */
function createWishlistRouter({ pool }) {
  const router = createSavedGamesRouter({ pool, table: 'wishlist' });

  router.post(
    '/:gameId/move-to-library',
    asyncHandler(async (req, res) => {
      const gameId = parseGameId(req.params.gameId);
      const status = parseOptionalStatus(req.body?.status) ?? 'want_to_play';

      const connection = await pool.getConnection();
      try {
        await connection.beginTransaction();

        const [rows] = await connection.execute(
          'SELECT * FROM wishlist WHERE user_id = ? AND game_id = ? FOR UPDATE',
          [req.userId, gameId]
        );
        const item = rows[0];
        if (!item) throw Errors.notFound('That game is not in your wishlist.');

        const values = [
          status, item.game_name, item.cover_url, item.rating, item.release_year, item.genre, item.platforms,
        ];
        await connection.execute(
          `INSERT INTO library (user_id, game_id, status, ${SNAPSHOT_COLUMNS.join(', ')})
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
           ON DUPLICATE KEY UPDATE status = ?, ${SNAPSHOT_COLUMNS.map((c) => `${c} = ?`).join(', ')}`,
          [req.userId, gameId, ...values, ...values]
        );
        await connection.execute('DELETE FROM wishlist WHERE user_id = ? AND game_id = ?', [
          req.userId,
          gameId,
        ]);
        await connection.commit();

        const [saved] = await pool.execute('SELECT * FROM library WHERE user_id = ? AND game_id = ?', [
          req.userId,
          gameId,
        ]);
        res.json(toSavedGameDto(saved[0]));
      } catch (err) {
        await connection.rollback();
        throw err;
      } finally {
        connection.release();
      }
    })
  );

  return router;
}

module.exports = { createSavedGamesRouter, createWishlistRouter };