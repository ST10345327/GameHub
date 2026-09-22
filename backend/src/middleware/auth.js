'use strict';

const jwt = require('jsonwebtoken');
const { Errors } = require('../utils/errors');

/**
 * Builds middleware that requires a valid "Authorization: Bearer <token>" header.
 * On success `req.userId` holds the numeric user id from the token.
 * Distinguishes an expired token (TOKEN_EXPIRED) from a missing/forged one (UNAUTHORIZED);
 * the app clears its saved session on either.
 */
function requireAuth(config) {
  return (req, res, next) => {
    const header = req.headers.authorization || '';
    const [scheme, token] = header.split(' ');
    if (scheme !== 'Bearer' || !token) return next(Errors.unauthorized());

    try {
      const payload = jwt.verify(token, config.jwtSecret, { algorithms: ['HS256'] });
      req.userId = Number(payload.sub);
      return next();
    } catch (err) {
      return next(err.name === 'TokenExpiredError' ? Errors.tokenExpired() : Errors.unauthorized());
    }
  };
}

/** Signs a login token for `userId`. */
function signToken(config, userId) {
  return jwt.sign({}, config.jwtSecret, {
    subject: String(userId),
    expiresIn: config.jwtExpiresIn,
    algorithm: 'HS256',
  });
}

module.exports = { requireAuth, signToken };