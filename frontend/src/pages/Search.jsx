import React, { useEffect, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { searchMovies } from '../api/movieApi';
import SearchBar      from '../components/search/SearchBar';
import FilterPanel    from '../components/search/FilterPanel';
import MovieGrid      from '../components/movies/MovieGrid';
import LoadingSpinner from '../components/common/LoadingSpinner';

/**
 * Search page — reads params from URL so results are shareable.
 * Re-fetches whenever URL search params change.
 */
export default function Search() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [movies,  setMovies]  = useState([]);
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);
  const [count,   setCount]   = useState(null);

  const q        = searchParams.get('q')        || '';
  const type     = searchParams.get('type')     || 'all';
  const genre    = searchParams.get('genre')    || '';
  const minRating= searchParams.get('minRating')|| '';
  const maxRating= searchParams.get('maxRating')|| '';
  const minYear  = searchParams.get('minYear')  || '';
  const maxYear  = searchParams.get('maxYear')  || '';
  const director = searchParams.get('director') || '';

  const doSearch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { q: q||undefined, type, genre:genre||undefined,
        minRating:minRating||undefined, maxRating:maxRating||undefined,
        minYear:minYear||undefined, maxYear:maxYear||undefined,
        director:director||undefined };
      const { data } = await searchMovies(params);
      setMovies(data);
      setCount(data.length);
    } catch (err) {
      setError(err.response?.data?.message || 'Search failed');
    } finally {
      setLoading(false);
    }
  }, [searchParams]);  // re-run when URL params change

  useEffect(() => { doSearch(); }, [doSearch]);

  const handleFilter = (filters) => {
    const next = new URLSearchParams(searchParams);
    const keys = ['minRating','maxRating','minYear','maxYear','director','genre'];
    keys.forEach((k) => {
      if (filters[k]) next.set(k, filters[k]);
      else next.delete(k);
    });
    setSearchParams(next);
  };

  return (
    <div className="container" style={{ paddingTop: '40px' }}>
      <div style={{ marginBottom: '24px' }}>
        <SearchBar initialQuery={q} initialType={type} />
      </div>

      <FilterPanel
        onFilter={handleFilter}
        initialValues={{ genre, minRating, maxRating, minYear, maxYear, director }}
      />

      {count !== null && (
        <p style={{ color: 'var(--text-muted)', fontSize: '14px', marginBottom: '20px' }}>
          {count} result{count !== 1 ? 's' : ''}
          {q ? ` for "${q}"` : ''}
        </p>
      )}

      {loading && <LoadingSpinner message="Searching…" />}
      {error   && <div className="alert alert-error">{error}</div>}
      {!loading && <MovieGrid movies={movies} />}
    </div>
  );
}
