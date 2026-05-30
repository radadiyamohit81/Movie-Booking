import React, { useEffect, useState } from 'react';
import { getHome } from '../api/movieApi';
import MovieCarousel  from '../components/movies/MovieCarousel';
import SearchBar      from '../components/search/SearchBar';
import LoadingSpinner from '../components/common/LoadingSpinner';

/**
 * Home page — shows three carousels (Featured, Trending, Upcoming)
 * loaded from a single GET /api/home call.
 */
export default function Home() {
  const [data,    setData]    = useState({ featured: [], trending: [], upcoming: [] });
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    getHome()
      .then(({ data }) => setData(data))
      .catch(() => setError('Failed to load home data'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="container" style={{ paddingTop: '40px' }}>
      {/* Hero */}
      <section style={{ textAlign: 'center', padding: '60px 0 50px' }}>
        <h1 style={{ fontSize: '48px', fontWeight: 800, marginBottom: '12px' }}>
          Your World of <span style={{ color: 'var(--accent)' }}>Cinema</span>
        </h1>
        <p style={{ fontSize: '18px', color: 'var(--text-secondary)', marginBottom: '32px' }}>
          Explore movies and series, rate them, write reviews, build watchlists.
        </p>
        <div style={{ maxWidth: '700px', margin: '0 auto' }}>
          <SearchBar />
        </div>
      </section>

      {loading && <LoadingSpinner />}

      {error && <div className="alert alert-error">{error}</div>}

      {!loading && !error && (
        <>
          <MovieCarousel title="🔥 Featured"  movies={data.featured}  />
          <MovieCarousel title="📈 Trending"   movies={data.trending}  />
          <MovieCarousel title="🗓 Upcoming"  movies={data.upcoming}  />
        </>
      )}
    </div>
  );
}
