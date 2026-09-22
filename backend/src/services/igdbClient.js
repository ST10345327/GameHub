'use strict';

const API_BASE_URL = 'https://api.igdb.com/v4';
const OAUTH_URL = 'https://id.twitch.tv/oauth2/token';

/**
 * Small IGDB API client.
 *
 * The client:
 * 1. Gets a Twitch OAuth access token.
 * 2. Uses that token to call IGDB.
 * 3. Automatically refreshes the token when necessary.
 *
 * Credentials are supplied through config.igdb.
 */
class IgdbClient {
  constructor({ clientId, clientSecret }) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;

    this.accessToken = null;
    this.tokenExpiresAt = 0;

    this.isConfigured = Boolean(clientId && clientSecret);
  }

  /**
   * Gets a valid Twitch access token.
   */
  async getAccessToken() {
    if (!this.isConfigured) {
      throw new Error('IGDB is not configured.');
    }

    // Reuse the existing token if it has not expired.
    if (
      this.accessToken &&
      Date.now() < this.tokenExpiresAt
    ) {
      return this.accessToken;
    }

    const params = new URLSearchParams({
      client_id: this.clientId,
      client_secret: this.clientSecret,
      grant_type: 'client_credentials',
    });

    const response = await fetch(`${OAUTH_URL}?${params.toString()}`, {
      method: 'POST',
    });

    if (!response.ok) {
      const body = await response.text();
      throw new Error(
        `IGDB authentication failed (${response.status}): ${body}`
      );
    }

    const data = await response.json();

    this.accessToken = data.access_token;

    // Refresh slightly before the actual expiry time.
    const expiresInMs = Number(data.expires_in || 3600) * 1000;
    this.tokenExpiresAt = Date.now() + expiresInMs - 60_000;

    return this.accessToken;
  }

  /**
   * Sends an Apicalypse query to IGDB.
   *
   * Example:
   *   igdb.query('games', 'fields name; limit 10;')
   */
  async query(endpoint, queryBody) {
    if (!this.isConfigured) {
      const error = new Error('IGDB is not configured.');
      error.status = 503;
      throw error;
    }

    const accessToken = await this.getAccessToken();

    const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
      method: 'POST',
      headers: {
        'Client-ID': this.clientId,
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'text/plain',
      },
      body: queryBody,
    });

    // If the token has expired unexpectedly, clear it and retry once.
    if (response.status === 401) {
      this.accessToken = null;
      this.tokenExpiresAt = 0;

      const newToken = await this.getAccessToken();

      const retryResponse = await fetch(`${API_BASE_URL}/${endpoint}`, {
        method: 'POST',
        headers: {
          'Client-ID': this.clientId,
          Authorization: `Bearer ${newToken}`,
          'Content-Type': 'text/plain',
        },
        body: queryBody,
      });

      if (!retryResponse.ok) {
        const body = await retryResponse.text();
        throw new Error(
          `IGDB request failed (${retryResponse.status}): ${body}`
        );
      }

      return retryResponse.json();
    }

    if (!response.ok) {
      const body = await response.text();

      const error = new Error(
        `IGDB request failed (${response.status}): ${body}`
      );
      error.status = response.status;
      throw error;
    }

    return response.json();
  }
}

module.exports = { IgdbClient };