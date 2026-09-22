'use strict';

require('dotenv').config();

const { loadConfig } = require('./config');
const { createPool } = require('./db');
const { IgdbClient } = require('./services/igdbClient');
const { createGameService } = require('./services/gameService');
const { createApp } = require('./app');

const config = loadConfig();
const pool = createPool(config.db);
const igdb = new IgdbClient(config.igdb);
if (!igdb.isConfigured) {
  console.warn('[startup] IGDB_CLIENT_ID / IGDB_CLIENT_SECRET not set: /games endpoints will answer 503.');
}

const app = createApp({ config, pool, gameService: createGameService({ igdb }) });
const server = app.listen(config.port, () => {
  console.log(`GameHub API listening on port ${config.port} (${config.env})`);
});

// Finish in-flight requests and close DB connections when the host stops the process.
function shutdown(signal) {
  console.log(`${signal} received, shutting down...`);
  server.close(async () => {
    await pool.end();
    process.exit(0);
  });
}
process.on('SIGTERM', () => shutdown('SIGTERM'));
process.on('SIGINT', () => shutdown('SIGINT'));