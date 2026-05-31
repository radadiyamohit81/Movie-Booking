import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const TYPES = [
  { value: 'all',    label: 'All' },
  { value: 'title',  label: 'Titles' },
  { value: 'celebs', label: 'People' },
];

/**
 * Hero search bar used on the home and search pages.
 * Syncs with URL query params so results are shareable.
 */
export default function SearchBar({ initialQuery = '', initialType = 'all' }) {
  const [query, setQuery] = useState(initialQuery);
  const [type,  setType]  = useState(initialType);
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    const params = new URLSearchParams();
    if (query.trim()) params.set('q', query.trim());
    params.set('type', type);
    navigate(`/search?${params.toString()}`);
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
      {/* Type selector */}
      <div style={{ display: 'flex', background: 'var(--bg-card)', borderRadius: 'var(--radius)', overflow: 'hidden', border: '1px solid var(--border)' }}>
        {TYPES.map((t) => (
          <button
            key={t.value}
            type="button"
            onClick={() => setType(t.value)}
            style={{
              padding: '10px 14px',
              fontSize: '13px',
              fontWeight: 600,
              border: 'none',
              background: type === t.value ? 'var(--accent)' : 'transparent',
              color: type === t.value ? '#000' : 'var(--text-secondary)',
              cursor: 'pointer',
              transition: 'all var(--transition)',
            }}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Query input */}
      <input
        className="form-control"
        style={{ flex: 1, minWidth: '200px' }}
        placeholder={type === 'celebs' ? 'Search actors, directors…' : 'Search movies, shows…'}
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />

      <button type="submit" className="btn btn-primary">
        🔍 Search
      </button>
    </form>
  );
}
