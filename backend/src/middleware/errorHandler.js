'use strict';

const { ApiError, Errors } = require('../utils/errors');

/** Fallback for routes that do not exist. */
function notFoundHandler(req, res, next) {
  next(Errors.notFound(`No route for ${req.method} ${req.path}`));
}

/**
 * Central error handler. Known ApiErrors are returned as `{ error: { code, message, details } }`.
 * Anything else is logged server-side and answered with a generic message so internals
 * (SQL, stack traces) never reach the client.
 */
// eslint-disable-next-line no-unused-vars
function errorHandler(err, req, res, next) {
  // Malformed JSON body from express.json()
  if (err.type === 'entity.parse.failed') {
    err = new ApiError(400, 'VALIDATION_ERROR', 'Request body is not valid JSON.');
  }
  if (err.type === 'entity.too.large') {
    err = new ApiError(413, 'VALIDATION_ERROR', 'Request body is too large.');
  }

  if (err instanceof ApiError) {
    return res.status(err.status).json({
      error: { code: err.code, message: err.message, details: err.details },
    });
  }

  console.error(`[error] ${req.method} ${req.originalUrl}`, err);
  return res.status(500).json({
    error: { code: 'INTERNAL_ERROR', message: 'Something went wrong on our side. Please try again.' },
  });
}

module.exports = { notFoundHandler, errorHandler };