import React from 'react';
import { formatDate, truncate } from '../../utils/helpers';

export default function ReviewCard({ review, currentUserId, onDelete }) {
  const isSpoiler = review.spoiler;
  const [showSpoiler, setShowSpoiler] = React.useState(false);
  const canDelete = currentUserId && review.userId === currentUserId;

  return (
    <div style={{
      background: 'var(--bg-card)',
      border: '1px solid var(--border)',
      borderRadius: 'var(--radius)',
      padding: '16px',
      position: 'relative',
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{
            width: '34px', height: '34px', borderRadius: '50%',
            background: 'var(--accent)', display: 'flex', alignItems: 'center',
            justifyContent: 'center', fontSize: '14px', fontWeight: 700, color: '#000',
          }}>
            {review.userId?.toString().slice(0,1) || 'U'}
          </div>
          <div>
            <div style={{ fontSize: '13px', fontWeight: 600 }}>User #{review.userId}</div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{formatDate(review.createdAt)}</div>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          {isSpoiler && (
            <span className="badge" style={{ background: 'rgba(231,76,60,0.2)', color: '#e74c3c', border: '1px solid rgba(231,76,60,0.3)' }}>
              SPOILER
            </span>
          )}
          {canDelete && (
            <button
              className="btn btn-danger btn-sm"
              onClick={() => onDelete(review.id)}
            >
              🗑
            </button>
          )}
        </div>
      </div>

      {/* Content */}
      {isSpoiler && !showSpoiler ? (
        <div style={{ textAlign: 'center', padding: '16px' }}>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px', marginBottom: '10px' }}>
            This review contains spoilers.
          </p>
          <button className="btn btn-secondary btn-sm" onClick={() => setShowSpoiler(true)}>
            Show Spoiler
          </button>
        </div>
      ) : (
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.7 }}>
          {review.content}
        </p>
      )}
    </div>
  );
}
