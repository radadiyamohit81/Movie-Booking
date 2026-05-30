import api from './axios';

// ── Public ────────────────────────────────────────────────────────────────────
export const getHome      = ()              => api.get('/api/home');
export const getMovie     = (id)            => api.get(`/api/movies/${id}`);
export const getFeatured  = (limit = 10)   => api.get(`/api/movies/featured?limit=${limit}`);
export const getByType    = (type)          => api.get(`/api/movies/type/${type}`);

/**
 * Search with all optional params.
 * Strips undefined/null/empty values so the URL stays clean.
 */
export const searchMovies = (params) => {
  const clean = Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== null && v !== undefined && v !== '')
  );
  return api.get('/api/movies/search', { params: clean });
};

// ── Protected ─────────────────────────────────────────────────────────────────
export const getWatchlist    = ()     => api.get('/api/watchlist');
export const addToWatchlist  = (movieId) => api.post('/api/watchlist', { movieId });
export const removeFromWatchlist = (movieId) => api.delete(`/api/watchlist/${movieId}`);

export const getRatings  = (movieId)  => api.get(`/api/ratings/movie/${movieId}`);
export const rateMovie   = (data)     => api.post('/api/ratings', data);
export const deleteRating = (id)      => api.delete(`/api/ratings/${id}`);

export const getReviews  = (movieId)  => api.get(`/api/reviews/movie/${movieId}`);
export const createReview = (data)    => api.post('/api/reviews', data);
export const deleteReview = (id)      => api.delete(`/api/reviews/${id}`);

// ── Admin ─────────────────────────────────────────────────────────────────────
export const adminCreateMovie = (data)    => api.post('/api/admin/movies', data);
export const adminUpdateMovie = (id, data) => api.put(`/api/admin/movies/${id}`, data);
export const adminDeleteMovie = (id)      => api.delete(`/api/admin/movies/${id}`);
