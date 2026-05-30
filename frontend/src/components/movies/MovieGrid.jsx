import React from 'react';
import MovieCard from './MovieCard';
import '../../components/movies/MovieCard.css';

export default function MovieGrid({ movies }) {
  if (!movies?.length) {
    return (
      <div className="empty-state">
        <h3>No movies found</h3>
        <p>Try adjusting your search or filters.</p>
      </div>
    );
  }

  return (
    <div className="movie-grid">
      {movies.map((movie) => (
        <MovieCard key={movie._id} movie={movie} />
      ))}
    </div>
  );
}
