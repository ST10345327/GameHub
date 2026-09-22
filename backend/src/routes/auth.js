'use strict';

const express = require('express');
const bcrypt = require('bcryptjs');
const { asyncHandler, Errors } = require('../utils/errors');
const { requireAuth, signToken } = require('../middleware/auth');
const { toUserDto } = require('../utils/mappers');
const {
  parseRegistration,
  parseLogin,
  parseChangePassword,
  validateEmail,
} = require('../utils/validation');

/**
 * Account endpoints:
 *   POST /auth/register          create an account, returns { token, user }
 *   POST /auth/login             returns { token, user }
 *   POST /auth/forgot-password   always answers 200 (does not reveal whether the email exists)
 *   POST /auth/change-password   (needs a token)
 */
function createAuthRouter({ pool, config }) {
  const router = express.Router();

  // A real bcrypt hash of a random string. When the email is unknown we still run a compare
  // against it so response time does not reveal which emails are registered.
  const dummyHash = bcrypt.hashSync('gamehub-dummy-password', config.bcryptRounds);

  router.post(
    '/register',
    asyncHandler(async (req, res) => {
      const { username, email, password } = parseRegistration(req.body);
      const passwordHash = await bcrypt.hash(password, config.bcryptRounds);

      try {
        const [result] = await pool.execute(
          'INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)',
          [username, email, passwordHash]
        );
        const [rows] = await pool.execute('SELECT * FROM users WHERE user_id = ?', [result.insertId]);
        const user = rows[0];
        return res.status(201).json({ token: signToken(config, user.user_id), user: toUserDto(user) });
      } catch (err) {
        if (err.code === 'ER_DUP_ENTRY') {
          // The violated unique key tells us which field clashed.
          throw String(err.sqlMessage).includes('uq_users_username')
            ? Errors.usernameTaken()
            : Errors.emailTaken();
        }
        throw err;
      }
    })
  );

  router.post(
    '/login',
    asyncHandler(async (req, res) => {
      const { email, password } = parseLogin(req.body);
      const [rows] = await pool.execute('SELECT * FROM users WHERE email = ?', [email]);
      const user = rows[0];

      const matches = await bcrypt.compare(password, user ? user.password_hash : dummyHash);
      if (!user || !matches) throw Errors.invalidCredentials();

      await pool.execute('UPDATE users SET last_active = UTC_TIMESTAMP() WHERE user_id = ?', [user.user_id]);
      res.json({ token: signToken(config, user.user_id), user: toUserDto(user) });
    })
  );

  router.post(
    '/forgot-password',
    asyncHandler(async (req, res) => {
      const emailError = validateEmail(req.body?.email);
      if (emailError) throw Errors.validation({ email: emailError });
      // Prototype: no email service is connected. A production build would generate a
      // single-use reset token here and email it. The response is identical whether or not
      // the account exists, to avoid leaking which emails are registered.
      console.log('[auth] password reset requested (email delivery not configured)');
      res.json({ message: 'If an account exists for that email, reset instructions have been sent.' });
    })
  );

  router.post(
    '/change-password',
    requireAuth(config),
    asyncHandler(async (req, res) => {
      const { currentPassword, newPassword } = parseChangePassword(req.body);
      const [rows] = await pool.execute('SELECT password_hash FROM users WHERE user_id = ?', [req.userId]);
      if (!rows[0]) throw Errors.unauthorized();

      const matches = await bcrypt.compare(currentPassword, rows[0].password_hash);
      if (!matches) throw Errors.wrongPassword();

      const newHash = await bcrypt.hash(newPassword, config.bcryptRounds);
      await pool.execute('UPDATE users SET password_hash = ? WHERE user_id = ?', [newHash, req.userId]);
      res.json({ message: 'Password updated.' });
    })
  );

  return router;
}

module.exports = { createAuthRouter };