import React from 'react';

export default function LoadingSpinner({ message = 'Loading…' }) {
  return (
    <div style={{ textAlign: 'center', padding: '80px 24px' }}>
      <div className="spinner" />
      <p style={{ color: 'var(--text-muted)', marginTop: '16px', fontSize: '14px' }}>{message}</p>
    </div>
  );
}
