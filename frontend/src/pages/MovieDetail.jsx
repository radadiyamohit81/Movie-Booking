import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getMovie, getReviews, createReview, deleteReview,
  rateMovie, getRatings, deleteRating,
  addToWatchlist, removeFromWatchlist, getWatchlist,
} from '../api/movieApi';
import { useAuth }    from '../context/AuthContext';
import ReviewCard     from '../components/reviews/ReviewCard';
import ReviewForm     from '../components/reviews/ReviewForm';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { formatRating, posterGradient } from '../utils/helpers';

export default function MovieDetail() {
  const { id } = useParams();
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const [movie,     setMovie]     = useState(null);
  const [reviews,   setReviews]   = useState([]);
  const [ratings,   setRatings]   = useState([]);
  const [inWatchlist, setInWatchlist] = useState(false);
  const [loading,   setLoading]   = useState(true);
  const [error,     setError]     = useState(null);
  const [reviewLoading, setReviewLoading] = useState(false);
  const [userRating, setUserRating] = useState('');
  const [msg, setMsg] = useState('');

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        const [movieRes, reviewRes, ratingRes] = await Promise.all([
          getMovie(id),
          getReviews(id),
          getRatings(id),
        ]);
        setMovie(movieRes.data);
        setReviews(reviewRes.data);
        setRatings(ratingRes.data);

        if (isAuthenticated) {
          const wlRes = await getWatchlist();
          setInWatchlist(wlRes.data.some((w) => String(w.movieId) === String(id)));
        }
      } catch {
        setError('Failed to load movie details');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [id, isAuthenticated]);

  const handleRating = async () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    try {
      await rateMovie({ movieId: Number(id), rating: Number(userRating) });
      const res = await getRatings(id);
      setRatings(res.data);
      setMsg('Rating submitted!');
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to submit rating');
    }
  };

  const handleReview = async (data) => {
    if (!isAuthenticated) { navigate('/login'); return; }
    setReviewLoading(true);
    try {
      await createReview({ movieId: Number(id), ...data });
      const res = await getReviews(id);
      setReviews(res.data);
      setMsg('Review submitted!');
    } catch (e) {
      setMsg(e.response?.data?.message || 'Failed to submit review');
    } finally {
      setReviewLoading(false);
    }
  };

  const handleDeleteReview = async (reviewId) => {
    await deleteReview(reviewId);
    setReviews((prev) => prev.filter((r) => r.id !== reviewId));
  };

  const toggleWatchlist = async () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    try {
      if (inWatchlist) {
        await removeFromWatchlist(id);
        setInWatchlist(false);
      } else {
        await addToWatchlist(Number(id));
        setInWatchlist(true);
      }
    } catch (e) {
      setMsg(e.response?.data?.message || 'Watchlist update failed');
    }
  };

  if (loading) return <LoadingSpinner />;
  if (error)   return <div className="container" style={{paddingTop:'60px'}}><div className="alert alert-error">{error}</div></div>;
  if (!movie)  return null;

  const avgRating = ratings.length
    ? (ratings.reduce((s, r) => s + r.rating, 0) / ratings.length).toFixed(1)
    : formatRating(movie.rating);

  return (
    <div className="container" style={{ paddingTop: '40px', paddingBottom: '60px' }}>
      {/* Hero */}
      <div style={{ display: 'flex', gap: '32px', flexWrap: 'wrap', marginBottom: '40px' }}>
        {/* Poster */}
        <div style={{
          width: '240px', flexShrink: 0, aspectRatio: '2/3',
          borderRadius: 'var(--radius-lg)', background: posterGradient(movie._id),
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '64px',
        }}>
          🎬
        </div>

        {/* Info */}
        <div style={{ flex: 1, minWidth: '260px' }}>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '12px' }}>
            {movie.genre?.map((g) => <span key={g} className="badge badge-genre">{g}</span>)}
            <span className="badge badge-type">{movie.type}</span>
          </div>

          <h1 style={{ fontSize: '36px', fontWeight: 800, marginBottom: '8px' }}>{movie.title}</h1>

          <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', marginBottom: '16px', color: 'var(--text-secondary)', fontSize: '15px' }}>
            <span>{movie.year}</span>
            {movie.duration && <span>⏱ {movie.duration}</span>}
            <span className="star-rating">★ {avgRating}</span>
            <span style={{ color:'var(--text-muted)', fontSize:'13px' }}>({ratings.length} ratings)</span>
          </div>

          <p style={{ color: 'var(--text-secondary)', lineHeight: 1.8, marginBottom: '20px', maxWidth: '600px' }}>
            {movie.description}
          </p>

          {movie.director && (
            <p style={{ fontSize: '14px', color:'var(--text-muted)', marginBottom: '8px' }}>
              <strong style={{color:'var(--text-primary)'}}>Director:</strong> {movie.director}
            </p>
          )}
          {movie.stars?.length > 0 && (
            <p style={{ fontSize: '14px', color:'var(--text-muted)', marginBottom: '20px' }}>
              <strong style={{color:'var(--text-primary)'}}>Stars:</strong> {movie.stars.join(', ')}
            </p>
          )}

          {/* Action buttons */}
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <button className={`btn ${inWatchlist ? 'btn-secondary' : 'btn-primary'}`} onClick={toggleWatchlist}>
              {inWatchlist ? '✓ In Watchlist' : '+ Watchlist'}
            </button>

            {isAuthenticated && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <select
                  value={userRating}
                  onChange={(e) => setUserRating(e.target.value)}
                  className="form-control"
                  style={{ width: '90px', padding: '10px' }}
                >
                  <option value="">Rate</option>
                  {[...Array(10)].map((_, i) => {
                    const val = (i + 1).toFixed(1);
                    return <option key={val} value={val}>★ {val}</option>;
                  })}
                </select>
                <button className="btn btn-primary" onClick={handleRating} disabled={!userRating}>
                  Submit
                </button>
              </div>
            )}
          </div>

          {msg && <div className="alert alert-success" style={{ marginTop: '12px' }}>{msg}</div>}
        </div>
      </div>

      {/* Reviews */}
      <section>
        <h2 className="section-title">Reviews ({reviews.length})</h2>

        {isAuthenticated && (
          <div style={{ marginBottom: '24px' }}>
            <ReviewForm onSubmit={handleReview} loading={reviewLoading} />
          </div>
        )}

        {reviews.length === 0 ? (
          <div className="empty-state"><h3>No reviews yet</h3><p>Be the first to review this movie.</p></div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {reviews.map((r) => (
              <ReviewCard
                key={r.id}
                review={r}
                currentUserId={null}   /* userId comparison done server-side */
                onDelete={handleDeleteReview}
              />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
