'use strict';

/**
 * Tiny in-memory cache with per-entry expiry. Used to avoid hitting IGDB (rate-limited to
 * about 4 requests/second) every time the home screen loads the same discovery rows.
 * `now` is injectable so tests can move the clock without waiting.
 */
class TtlCache {
  constructor(ttlMs, now = () => Date.now()) {
    this.ttlMs = ttlMs;
    this.now = now;
    this.entries = new Map();
  }

  get(key) {
    const entry = this.entries.get(key);
    if (!entry) return undefined;
    if (entry.expiresAt <= this.now()) {
      this.entries.delete(key);
      return undefined;
    }
    return entry.value;
  }

  set(key, value) {
    this.entries.set(key, { value, expiresAt: this.now() + this.ttlMs });
  }
}

module.exports = { TtlCache };