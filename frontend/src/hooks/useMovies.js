import { useState, useCallback } from 'react';
import { searchMovies, getFeatured } from '../api/movieApi';

/**
 * Custom hook encapsulating movie-fetching logic.
 * Keeps components declarative — they only call fetchMovies() and read state.
 */
export function useMovies() {
  const [movies,  setMovies]  = useState([]);
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);

  const fetchMovies = useCallback(async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await searchMovies(params);
      setMovies(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load movies');
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchFeatured = useCallback(async (limit = 10) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await getFeatured(limit);
      setMovies(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load featured movies');
    } finally {
      setLoading(false);
    }
  }, []);

  return { movies, loading, error, fetchMovies, fetchFeatured };
}
