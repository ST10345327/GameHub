'use strict';

/**
 * Creates the tables from sql/schema.sql on the database named in .env.
 * Safe to run more than once (every statement uses CREATE TABLE IF NOT EXISTS).
 * Usage: npm run db:init
 */
require('dotenv').config();
const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

async function main() {
  const { DB_HOST, DB_PORT, DB_USER, DB_PASSWORD, DB_NAME, DB_SSL } = process.env;
  if (!DB_HOST || !DB_USER || !DB_NAME) {
    throw new Error('Set DB_HOST, DB_USER and DB_NAME (and usually DB_PASSWORD) in backend/.env first.');
  }

  const connection = await mysql.createConnection({
    host: DB_HOST,
    port: Number(DB_PORT) || 3306,
    user: DB_USER,
    password: DB_PASSWORD ?? '',
    database: DB_NAME,
    ssl: DB_SSL === 'true' ? { rejectUnauthorized: true } : undefined,
    multipleStatements: true,
  });

  try {
    const sql = fs.readFileSync(path.join(__dirname, '..', 'sql', 'schema.sql'), 'utf8');
    await connection.query(sql);
    const [tables] = await connection.query('SHOW TABLES');
    console.log('Database ready. Tables:', tables.map((row) => Object.values(row)[0]).join(', '));
  } finally {
    await connection.end();
  }
}

main().catch((err) => {
  console.error('Database setup failed:', err.message);
  process.exit(1);
});