'use strict';

const { Errors } = require('../utils/errors');
const { GENRE_FILTERS, PLATFORM_FILTERS, SORT_OPTIONS } = require('./igdbFilters');

const has = (object, key) => Object.prototype.hasOwnProperty.call(object, key);

/** Parses an optional integer query parameter; records a message in `errors` if it is invalid. */
function optionalInt(value, name, { min, max }, errors) {
  if (value === undefined || value === '') return undefined;
  if (!/^-?\d+$/.test(String(value))) {
    errors[name] = 'Must be a whole number.';
    return undefined;
  }
  const number = Number(value);
  if (number < min || number > max) {
    errors[name] = `Must be between ${min} and ${max}.`;
    return undefined;
  }
  return number;
}

/** Filters shared by search and Surprise Me: genre, platform and minimum rating. */
function parseFilters(query, errors) {
  const filters = {};

  if (query.genre !== undefined && query.genre !== '') {
    if (has(GENRE_FILTERS, query.genre)) filters.genre = query.genre;
    else errors.genre = `Must be one of: ${Object.keys(GENRE_FILTERS).join(', ')}.`;
  }
  if (query.platform !== undefined && query.platform !== '') {
    if (has(PLATFORM_FILTERS, query.platform)) filters.platform = query.platform;
    else errors.platform = `Must be one of: ${Object.keys(PLATFORM_FILTERS).join(', ')}.`;
  }
  filters.minRating = optionalInt(query.minRating, 'minRating', { min: 0, max: 10 }, errors);
  return filters;
}

/** Validates the query string of GET /games/search. */
function parseSearchParams(query = {}) {
  const errors = {};
  const params = parseFilters(query, errors);

  if (query.q !== undefined) {
    const q = String(query.q).trim();
    if (q.length > 100) errors.q = 'Must be at most 100 characters.';
    else if (q.length > 0) params.q = q;
  }

  params.releasedAfter = optionalInt(query.releasedAfter, 'releasedAfter', { min: 1950, max: 2100 }, errors);
  params.releasedBefore = optionalInt(query.releasedBefore, 'releasedBefore', { min: 1950, max: 2100 }, errors);
  params.limit = optionalInt(query.limit, 'limit', { min: 1, max: 50 }, errors) ?? 20;
  params.offset = optionalInt(query.offset, 'offset', { min: 0, max: 1000 }, errors) ?? 0;

  if (query.sort !== undefined && query.sort !== '') {
    if (SORT_OPTIONS.includes(query.sort)) params.sort = query.sort;
    else errors.sort = `Must be one of: ${SORT_OPTIONS.join(', ')}.`;
  }

  if (Object.keys(errors).length > 0) throw Errors.validation(errors);
  return params;
}

/** Validates the query string of GET /games/random. */
function parseRandomParams(query = {}) {
  const errors = {};
  const params = parseFilters(query, errors);
  if (Object.keys(errors).length > 0) throw Errors.validation(errors);
  return params;
}

module.exports = { parseSearchParams, parseRandomParams };