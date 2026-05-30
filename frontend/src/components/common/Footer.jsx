import React from 'react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer style={{
      background: 'var(--bg-secondary)',
      borderTop: '1px solid var(--border)',
      padding: '40px 24px',
      marginTop: '60px',
    }}>
      <div className="container" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
        <div style={{ fontSize: '24px', fontWeight: 800, color: 'var(--accent)' }}>🎬 MovieDB</div>
        <div style={{ display: 'flex', gap: '24px', flexWrap: 'wrap', justifyContent: 'center' }}>
          <Link to="/"        style={{ color: 'var(--text-muted)', fontSize: '14px' }}>Home</Link>
          <Link to="/search"  style={{ color: 'var(--text-muted)', fontSize: '14px' }}>Browse</Link>
          <Link to="/login"   style={{ color: 'var(--text-muted)', fontSize: '14px' }}>Sign In</Link>
          <Link to="/register" style={{ color: 'var(--text-muted)', fontSize: '14px' }}>Sign Up</Link>
        </div>
        <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
          © {new Date().getFullYear()} MovieDB. Built with Spring Boot + React.
        </p>
      </div>
    </footer>
  );
}
