import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { login } = useAuth();
  const navigate  = useNavigate();
  const [form,  setForm]  = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(form);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 64px)' }}>
      <div style={{ width: '100%', maxWidth: '420px', padding: '0 24px' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ fontSize: '40px', marginBottom: '8px' }}>🎬</div>
          <h1 style={{ fontSize: '28px', fontWeight: 800 }}>Welcome back</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '14px' }}>Sign in to your MovieDB account</p>
        </div>

        <form onSubmit={handleSubmit}>
          {error && <div className="alert alert-error">{error}</div>}

          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username" name="username" type="text"
              className="form-control" placeholder="Enter your username"
              value={form.username} onChange={handleChange} required autoFocus
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password" name="password" type="password"
              className="form-control" placeholder="Enter your password"
              value={form.password} onChange={handleChange} required
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', justifyContent: 'center', padding: '14px' }} disabled={loading}>
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: '24px', fontSize: '14px', color: 'var(--text-muted)' }}>
          Don't have an account?{' '}
          <Link to="/register" style={{ color: 'var(--accent)', fontWeight: 600 }}>Sign up</Link>
        </p>

        <p style={{ textAlign: 'center', marginTop: '12px', fontSize: '13px', color: 'var(--text-muted)' }}>
          Demo: <strong>testuser</strong> / test123 · Admin: <strong>admin</strong> / admin123
        </p>
      </div>
    </div>
  );
}
