import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getWatchlist, removeFromWatchlist, getMovie } from '../api/movieApi';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { formatRating, posterGradient } from '../utils/helpers';

export default function Watchlist() {
  const [items,   setItems]   = useState([]);
  const [movies,  setMovies]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    async function load() {
      try {
        const { data: watchlist } = await getWatchlist();
        setItems(watchlist);
        const movieDetails = await Promise.all(
          watchlist.map((w) => getMovie(w.movieId).then((r) => r.data))
        );
        setMovies(movieDetails);
      } catch {
        setError('Failed to load watchlist');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  const handleRemove = async (movieId) => {
    await removeFromWatchlist(movieId);
    setMovies((prev) => prev.filter((m) => m._id !== String(movieId)));
    setItems((prev) => prev.filter((w) => String(w.movieId) !== String(movieId)));
  };

  return (
    <div className="container" style={{ paddingTop: '40px', paddingBottom: '60px' }}>
      <h1 style={{ fontSize: '32px', fontWeight: 800, marginBottom: '32px' }}>📋 My Watchlist</h1>

      {loading && <LoadingSpinner />}
      {error   && <div className="alert alert-error">{error}</div>}

      {!loading && movies.length === 0 && (
        <div className="empty-state">
          <h3>Your watchlist is empty</h3>
          <p>Browse movies and add them to your watchlist.</p>
          <Link to="/search" className="btn btn-primary" style={{ marginTop: '16px', display: 'inline-flex' }}>Browse Movies</Link>
        </div>
      )}

      {!loading && movies.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {movies.map((movie) => (
            <div key={movie._id} style={{
              background: 'var(--bg-card)', border: '1px solid var(--border)',
              borderRadius: 'var(--radius)', padding: '16px',
              display: 'flex', alignItems: 'center', gap: '16px',
            }}>
              {/* Mini poster */}
              <div style={{
                width: '60px', height: '90px', borderRadius: '6px',
                background: posterGradient(movie._id), flexShrink: 0,
                display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '28px',
              }}>🎬</div>

              <div style={{ flex: 1, minWidth: 0 }}>
                <Link to={`/movie/${movie._id}`}>
                  <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '4px' }}>{movie.title}</h3>
                </Link>
                <div style={{ display: 'flex', gap: '12px', color: 'var(--text-muted)', fontSize: '13px', flexWrap: 'wrap' }}>
                  <span>{movie.year}</span>
                  <span className="star-rating" style={{ fontSize: '13px' }}>★ {formatRating(movie.rating)}</span>
                  {movie.genre?.slice(0,2).map((g) => <span key={g} className="badge badge-genre">{g}</span>)}
                </div>
              </div>

              <button
                className="btn btn-danger btn-sm"
                onClick={() => handleRemove(movie._id)}
              >
                Remove
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
