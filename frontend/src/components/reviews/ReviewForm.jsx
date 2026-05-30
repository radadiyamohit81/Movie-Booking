import React, { useState } from 'react';

export default function ReviewForm({ onSubmit, loading }) {
  const [content,  setContent]  = useState('');
  const [spoiler,  setSpoiler]  = useState(false);
  const [error,    setError]    = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) { setError('Review content is required.'); return; }
    if (content.length > 2000) { setError('Review must not exceed 2000 characters.'); return; }
    setError('');
    await onSubmit({ content: content.trim(), spoiler });
    setContent('');
    setSpoiler(false);
  };

  return (
    <form onSubmit={handleSubmit} style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '20px' }}>
      <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '16px' }}>Write a Review</h3>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="form-group">
        <label>Your Review</label>
        <textarea
          className="form-control"
          rows={5}
          placeholder="Share your thoughts about this movie…"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          maxLength={2000}
          style={{ resize: 'vertical' }}
        />
        <div style={{ fontSize: '12px', color: 'var(--text-muted)', textAlign: 'right' }}>
          {content.length}/2000
        </div>
      </div>

      <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer', marginBottom: '16px', fontSize: '14px', color: 'var(--text-secondary)' }}>
        <input
          type="checkbox"
          checked={spoiler}
          onChange={(e) => setSpoiler(e.target.checked)}
          style={{ accentColor: 'var(--accent)' }}
        />
        Mark as spoiler
      </label>

      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? 'Submitting…' : 'Submit Review'}
      </button>
    </form>
  );
}
