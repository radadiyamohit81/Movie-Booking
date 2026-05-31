import React, { useState } from 'react';
import { adminCreateMovie, adminUpdateMovie, adminDeleteMovie } from '../api/movieApi';
import { useMovies } from '../hooks/useMovies';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { useEffect } from 'react';

const EMPTY_FORM = {
  title: '', year: '', duration: '', rating: '', popularity: '',
  description: '', director: '', writers: '', stars: '', genre: '',
  type: 'movie', status: 'published',
};

export default function Admin() {
  const { movies, loading, error, fetchMovies } = useMovies();
  const [form,    setForm]    = useState(EMPTY_FORM);
  const [editId,  setEditId]  = useState(null);
  const [msg,     setMsg]     = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => { fetchMovies({}); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const toPayload = (f) => ({
    title:       f.title,
    year:        f.year       ? Number(f.year)       : undefined,
    duration:    f.duration   || undefined,
    rating:      f.rating     ? Number(f.rating)     : undefined,
    popularity:  f.popularity ? Number(f.popularity) : undefined,
    description: f.description || undefined,
    director:    f.director   || undefined,
    writers:     f.writers    ? f.writers.split(',').map((s) => s.trim()) : [],
    stars:       f.stars      ? f.stars.split(',').map((s) => s.trim())   : [],
    genre:       f.genre      ? f.genre.split(',').map((s) => s.trim())   : [],
    type:        f.type,
    status:      f.status,
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setMsg('');
    try {
      if (editId) {
        await adminUpdateMovie(editId, toPayload(form));
        setMsg('Movie updated successfully!');
      } else {
        await adminCreateMovie(toPayload(form));
        setMsg('Movie created successfully!');
      }
      setForm(EMPTY_FORM);
      setEditId(null);
      fetchMovies({});
    } catch (err) {
      setMsg(err.response?.data?.message || 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (movie) => {
    setEditId(movie._id);
    setForm({
      title:       movie.title       || '',
      year:        movie.year        || '',
      duration:    movie.duration    || '',
      rating:      movie.rating      || '',
      popularity:  movie.popularity  || '',
      description: movie.description || '',
      director:    movie.director    || '',
      writers:     (movie.writers  || []).join(', '),
      stars:       (movie.stars    || []).join(', '),
      genre:       (movie.genre    || []).join(', '),
      type:        movie.type   || 'movie',
      status:      movie.status || 'published',
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this movie permanently?')) return;
    await adminDeleteMovie(id);
    fetchMovies({});
    setMsg('Movie deleted.');
  };

  return (
    <div className="container" style={{ paddingTop: '40px', paddingBottom: '60px' }}>
      <h1 style={{ fontSize: '32px', fontWeight: 800, marginBottom: '32px' }}>⚙️ Admin Panel</h1>

      {/* Form */}
      <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: '28px', marginBottom: '40px' }}>
        <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '20px' }}>
          {editId ? '✏️ Edit Movie' : '➕ Add New Movie'}
        </h2>

        {msg && <div className={`alert ${msg.includes('failed') || msg.includes('Failed') ? 'alert-error' : 'alert-success'}`}>{msg}</div>}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px,1fr))', gap: '16px' }}>
            {[
              { name:'title',       label:'Title *',      placeholder:'The Dark Knight' },
              { name:'year',        label:'Year',         placeholder:'2008', type:'number' },
              { name:'duration',    label:'Duration',     placeholder:'2h 32m' },
              { name:'rating',      label:'Rating (0-10)', placeholder:'9.0', type:'number' },
              { name:'popularity',  label:'Popularity',   placeholder:'98', type:'number' },
              { name:'director',    label:'Director',     placeholder:'Christopher Nolan' },
            ].map(({ name, label, placeholder, type = 'text' }) => (
              <div key={name} className="form-group" style={{ margin: 0 }}>
                <label>{label}</label>
                <input name={name} type={type} className="form-control" placeholder={placeholder}
                  value={form[name]} onChange={handleChange} />
              </div>
            ))}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginTop: '16px' }}>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Writers (comma-separated)</label>
              <input name="writers" className="form-control" placeholder="Jonathan Nolan, Christopher Nolan"
                value={form.writers} onChange={handleChange} />
            </div>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Stars (comma-separated)</label>
              <input name="stars" className="form-control" placeholder="Christian Bale, Heath Ledger"
                value={form.stars} onChange={handleChange} />
            </div>
          </div>

          <div style={{ marginTop: '16px' }} className="form-group">
            <label>Genres (comma-separated)</label>
            <input name="genre" className="form-control" placeholder="Action, Crime, Drama"
              value={form.genre} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea name="description" className="form-control" rows={3}
              placeholder="Movie description…" value={form.description} onChange={handleChange}
              style={{ resize: 'vertical' }} />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Type</label>
              <select name="type" className="form-control" value={form.type} onChange={handleChange}>
                <option value="movie">Movie</option>
                <option value="series">Series</option>
              </select>
            </div>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Status</label>
              <select name="status" className="form-control" value={form.status} onChange={handleChange}>
                <option value="published">Published</option>
                <option value="upcoming">Upcoming</option>
                <option value="draft">Draft</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '10px', marginTop: '8px' }}>
            <button type="submit" className="btn btn-primary" disabled={submitting || !form.title}>
              {submitting ? 'Saving…' : editId ? 'Update Movie' : 'Create Movie'}
            </button>
            {editId && (
              <button type="button" className="btn btn-secondary" onClick={() => { setEditId(null); setForm(EMPTY_FORM); }}>
                Cancel
              </button>
            )}
          </div>
        </form>
      </div>

      {/* Movie list */}
      <h2 className="section-title">All Movies ({movies.length})</h2>
      {loading && <LoadingSpinner />}
      {error   && <div className="alert alert-error">{error}</div>}

      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
        {movies.map((movie) => (
          <div key={movie._id} style={{
            background: 'var(--bg-card)', border: '1px solid var(--border)',
            borderRadius: 'var(--radius)', padding: '14px 16px',
            display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px',
          }}>
            <div style={{ flex: 1, minWidth: 0 }}>
              <span style={{ fontWeight: 600, fontSize: '15px' }}>{movie.title}</span>
              <span style={{ marginLeft: '12px', color: 'var(--text-muted)', fontSize: '13px' }}>
                {movie.year} · ★ {movie.rating ?? '—'} · {movie.status}
              </span>
            </div>
            <div style={{ display: 'flex', gap: '8px', flexShrink: 0 }}>
              <button className="btn btn-secondary btn-sm" onClick={() => handleEdit(movie)}>Edit</button>
              <button className="btn btn-danger btn-sm"   onClick={() => handleDelete(movie._id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
