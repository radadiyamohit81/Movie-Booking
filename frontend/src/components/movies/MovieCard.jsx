import React from 'react';
import { Link } from 'react-router-dom';
import { formatRating, posterGradient, truncate } from '../../utils/helpers';

/**
 * Movie card shown in grids and carousels.
 * Uses a gradient placeholder instead of an image (no image CDN configured).
 */
export default function MovieCard({ movie }) {
  const { _id, title, year, rating, genre = [], type, duration } = movie;

  return (
    <Link to={`/movie/${_id}`} className="card movie-card">
      {/* Poster placeholder */}
      <div
        className="movie-card-poster"
        style={{ background: posterGradient(_id) }}
      >
        <div className="movie-card-rating">
          <span className="star">★</span>
          <span>{formatRating(rating)}</span>
        </div>
        <span className="movie-card-type">{type}</span>
      </div>

      {/* Info */}
      <div className="movie-card-info">
        <h3 className="movie-card-title" title={title}>{truncate(title, 32)}</h3>
        <div className="movie-card-meta">
          <span>{year}</span>
          {duration && <span className="dot">·</span>}
          {duration && <span>{duration}</span>}
        </div>
        <div className="movie-card-genres">
          {genre.slice(0, 2).map((g) => (
            <span key={g} className="badge badge-genre">{g}</span>
          ))}
        </div>
      </div>
    </Link>
  );
}
