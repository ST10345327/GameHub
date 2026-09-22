'use strict';

const IMAGE_BASE = 'https://images.igdb.com/igdb/image/upload';

/** Builds a full image URL from an IGDB image id. `size` is an IGDB size token such as t_cover_big. */
const imageUrl = (imageId, size = 't_cover_big') =>
  imageId ? `${IMAGE_BASE}/${size}/${imageId}.jpg` : null;

const names = (list) => (Array.isArray(list) ? list.map((item) => item && item.name).filter(Boolean) : []);

/** IGDB ratings are 0-100 with decimals; the app shows one decimal out of 10 (e.g. 8.8). */
const toTenScale = (rating) => (typeof rating === 'number' ? Math.round(rating) / 10 : null);

/** Converts an IGDB game record into the summary JSON used by cards and lists. */
function toGameSummary(raw) {
  const releaseDate = raw.first_release_date ? new Date(raw.first_release_date * 1000) : null;
  return {
    id: raw.id,
    name: raw.name,
    coverUrl: imageUrl(raw.cover && raw.cover.image_id),
    rating: toTenScale(raw.total_rating),
    releaseYear: releaseDate ? releaseDate.getUTCFullYear() : null,
    releaseDate: releaseDate ? releaseDate.toISOString().slice(0, 10) : null,
    genres: names(raw.genres),
    platforms: names(raw.platforms),
  };
}

/** Converts an IGDB game record into the full details JSON. */
function toGameDetails(raw) {
  const companies = Array.isArray(raw.involved_companies) ? raw.involved_companies : [];
  const namesWhere = (flag) =>
    companies.filter((c) => c[flag] && c.company && c.company.name).map((c) => c.company.name);

  return {
    ...toGameSummary(raw),
    description: raw.summary || null,
    developers: namesWhere('developer'),
    publishers: namesWhere('publisher'),
    screenshots: (raw.screenshots || [])
      .map((s) => imageUrl(s.image_id, 't_screenshot_big'))
      .filter(Boolean)
      .slice(0, 8),
    similarGames: (raw.similar_games || []).slice(0, 10).map((g) => ({
      id: g.id,
      name: g.name,
      coverUrl: imageUrl(g.cover && g.cover.image_id),
    })),
  };
}

module.exports = { imageUrl, toGameSummary, toGameDetails };