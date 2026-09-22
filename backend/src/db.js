'use strict';

const mysql = require('mysql2/promise');

/** Creates the shared MySQL connection pool. */
function createPool(dbConfig) {
  return mysql.createPool({
    host: dbConfig.host,
    port: dbConfig.port,
    user: dbConfig.user,
    password: dbConfig.password,
    database: dbConfig.database,
    ssl: dbConfig.ssl ? { rejectUnauthorized: true } : undefined,
    waitForConnections: true,
    connectionLimit: 10,
    charset: 'utf8mb4',
    timezone: 'Z',        // store and read DATETIME values as UTC
    decimalNumbers: true, // return DECIMAL columns (ratings) as numbers, not strings
  });
}

module.exports = { createPool };