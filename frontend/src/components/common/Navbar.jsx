import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

/**
 * Fixed top navigation bar.
 * Shows different links based on auth state and admin role.
 */
export default function Navbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [query, setQuery] = useState('');

  const handleSearch = (e) => {
    e.preventDefault();
    if (query.trim()) {
      navigate(`/search?q=${encodeURIComponent(query.trim())}&type=all`);
      setQuery('');
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        {/* Logo */}
        <Link to="/" className="navbar-logo">
          <span className="logo-icon">🎬</span>
          <span className="logo-text">MovieDB</span>
        </Link>

        {/* Quick search bar */}
        <form className="navbar-search" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Search movies, actors…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="navbar-search-input"
          />
          <button type="submit" className="navbar-search-btn" aria-label="Search">
            🔍
          </button>
        </form>

        {/* Desktop nav links */}
        <div className={`navbar-links ${menuOpen ? 'open' : ''}`}>
          <Link to="/search" className="nav-link" onClick={() => setMenuOpen(false)}>
            Browse
          </Link>
          <Link to="/search?type=series" className="nav-link" onClick={() => setMenuOpen(false)}>
            Series
          </Link>

          {isAuthenticated ? (
            <>
              <Link to="/watchlist" className="nav-link" onClick={() => setMenuOpen(false)}>
                Watchlist
              </Link>
              {isAdmin && (
                <Link to="/admin" className="nav-link nav-link-admin" onClick={() => setMenuOpen(false)}>
                  Admin
                </Link>
              )}
              <div className="nav-user">
                <Link to="/profile" className="nav-username">
                  👤 {user?.username}
                </Link>
                <button className="btn btn-secondary btn-sm" onClick={handleLogout}>
                  Logout
                </button>
              </div>
            </>
          ) : (
            <div className="nav-auth">
              <Link to="/login"    className="btn btn-secondary btn-sm">Sign In</Link>
              <Link to="/register" className="btn btn-primary   btn-sm">Sign Up</Link>
            </div>
          )}
        </div>

        {/* Mobile hamburger */}
        <button
          className="hamburger"
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="Toggle menu"
        >
          <span /><span /><span />
        </button>
      </div>
    </nav>
  );
}
