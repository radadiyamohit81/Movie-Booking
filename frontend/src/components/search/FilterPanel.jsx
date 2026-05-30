import React, { useState } from 'react';

const GENRES = [
  'Action','Adventure','Animation','Biography','Comedy','Crime',
  'Documentary','Drama','Fantasy','History','Horror','Music',
  'Mystery','Romance','Sci-Fi','Thriller','War',
];

/**
 * Collapsible filter panel for advanced search.
 * Calls onFilter(params) when Apply is clicked.
 */
export default function FilterPanel({ onFilter, initialValues = {} }) {
  const [minRating, setMinRating] = useState(initialValues.minRating || '');
  const [maxRating, setMaxRating] = useState(initialValues.maxRating || '');
  const [minYear,   setMinYear]   = useState(initialValues.minYear   || '');
  const [maxYear,   setMaxYear]   = useState(initialValues.maxYear   || '');
  const [director,  setDirector]  = useState(initialValues.director  || '');
  const [genres,    setGenres]    = useState(
    initialValues.genre ? initialValues.genre.split(',') : []
  );
  const [open, setOpen] = useState(false);

  const toggleGenre = (g) => {
    setGenres((prev) => prev.includes(g) ? prev.filter((x) => x !== g) : [...prev, g]);
  };

  const handleApply = () => {
    onFilter({
      minRating: minRating || undefined,
      maxRating: maxRating || undefined,
      minYear:   minYear   || undefined,
      maxYear:   maxYear   || undefined,
      director:  director  || undefined,
      genre:     genres.length ? genres.join(',') : undefined,
    });
  };

  const handleReset = () => {
    setMinRating(''); setMaxRating('');
    setMinYear('');   setMaxYear('');
    setDirector('');  setGenres([]);
    onFilter({});
  };

  return (
    <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', overflow: 'hidden', marginBottom: '24px' }}>
      <button
        onClick={() => setOpen(!open)}
        style={{ width: '100%', padding: '14px 20px', background: 'transparent', border: 'none', color: 'var(--text-primary)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontWeight: 600, fontSize: '14px', cursor: 'pointer' }}
      >
        <span>🎛 Advanced Filters {genres.length > 0 && <span style={{color:'var(--accent)',marginLeft:'6px'}}>{genres.length} genre(s)</span>}</span>
        <span style={{ transform: open ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }}>▾</span>
      </button>

      {open && (
        <div style={{ padding: '0 20px 20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>

          {/* Rating range */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Min Rating</label>
              <input className="form-control" type="number" min="0" max="10" step="0.1"
                placeholder="0.0" value={minRating} onChange={(e) => setMinRating(e.target.value)} />
            </div>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Max Rating</label>
              <input className="form-control" type="number" min="0" max="10" step="0.1"
                placeholder="10.0" value={maxRating} onChange={(e) => setMaxRating(e.target.value)} />
            </div>
          </div>

          {/* Year range */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group" style={{ margin: 0 }}>
              <label>From Year</label>
              <input className="form-control" type="number" min="1900" max="2030"
                placeholder="1990" value={minYear} onChange={(e) => setMinYear(e.target.value)} />
            </div>
            <div className="form-group" style={{ margin: 0 }}>
              <label>To Year</label>
              <input className="form-control" type="number" min="1900" max="2030"
                placeholder="2024" value={maxYear} onChange={(e) => setMaxYear(e.target.value)} />
            </div>
          </div>

          {/* Director */}
          <div className="form-group" style={{ margin: 0 }}>
            <label>Director</label>
            <input className="form-control" type="text" placeholder="e.g. Nolan"
              value={director} onChange={(e) => setDirector(e.target.value)} />
          </div>

          {/* Genres */}
          <div>
            <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', marginBottom: '10px' }}>Genres</label>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {GENRES.map((g) => (
                <button
                  key={g}
                  type="button"
                  onClick={() => toggleGenre(g)}
                  style={{
                    padding: '4px 12px', borderRadius: '20px', fontSize: '13px', fontWeight: 500, cursor: 'pointer', border: '1px solid',
                    background: genres.includes(g) ? 'var(--accent)' : 'transparent',
                    color:      genres.includes(g) ? '#000' : 'var(--text-secondary)',
                    borderColor: genres.includes(g) ? 'var(--accent)' : 'var(--border)',
                    transition: 'all 0.15s',
                  }}
                >{g}</button>
              ))}
            </div>
          </div>

          {/* Actions */}
          <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
            <button className="btn btn-secondary btn-sm" onClick={handleReset}>Reset</button>
            <button className="btn btn-primary   btn-sm" onClick={handleApply}>Apply Filters</button>
          </div>
        </div>
      )}
    </div>
  );
}
