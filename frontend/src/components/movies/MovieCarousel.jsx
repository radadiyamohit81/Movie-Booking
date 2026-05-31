import React, { useRef } from 'react';
import MovieCard from './MovieCard';
import '../../components/movies/MovieCard.css';

/**
 * Horizontally scrollable movie carousel with arrow controls.
 */
export default function MovieCarousel({ title, movies = [] }) {
  const scrollRef = useRef(null);

  const scroll = (dir) => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: dir * 600, behavior: 'smooth' });
    }
  };

  if (!movies.length) return null;

  return (
    <section style={{ marginBottom: '40px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
        <h2 className="section-title" style={{ marginBottom: 0, flex: 1 }}>{title}</h2>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button className="btn btn-secondary btn-sm" onClick={() => scroll(-1)} aria-label="Scroll left">‹</button>
          <button className="btn btn-secondary btn-sm" onClick={() => scroll(1)}  aria-label="Scroll right">›</button>
        </div>
      </div>

      <div
        ref={scrollRef}
        style={{
          display: 'flex',
          gap: '16px',
          overflowX: 'auto',
          paddingBottom: '12px',
          scrollbarWidth: 'thin',
          scrollbarColor: 'var(--border) transparent',
        }}
      >
        {movies.map((movie) => (
          <div key={movie._id} style={{ minWidth: '180px', maxWidth: '180px' }}>
            <MovieCard movie={movie} />
          </div>
        ))}
      </div>
    </section>
  );
}
