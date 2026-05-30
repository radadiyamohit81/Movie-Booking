import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar          from './components/common/Navbar';
import Footer          from './components/common/Footer';
import ProtectedRoute  from './components/common/ProtectedRoute';

import Home        from './pages/Home';
import Search      from './pages/Search';
import MovieDetail from './pages/MovieDetail';
import Login       from './pages/Login';
import Register    from './pages/Register';
import Watchlist   from './pages/Watchlist';
import Profile     from './pages/Profile';
import Admin       from './pages/Admin';

/**
 * App root — wraps everything in:
 *   BrowserRouter  → HTML5 history routing (no hash fragments)
 *   AuthProvider   → global JWT auth state accessible via useAuth() hook
 *
 * Route structure:
 *   /              → Home (public)
 *   /search        → Search with filters (public)
 *   /movie/:id     → Movie detail (public)
 *   /login         → Login form (public)
 *   /register      → Registration form (public)
 *   /watchlist     → Protected — requires JWT
 *   /profile       → Protected — requires JWT
 *   /admin         → Protected — requires ROLE_ADMIN
 */
export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <main className="page-wrapper">
          <Routes>
            <Route path="/"           element={<Home />}        />
            <Route path="/search"     element={<Search />}      />
            <Route path="/movie/:id"  element={<MovieDetail />} />
            <Route path="/login"      element={<Login />}       />
            <Route path="/register"   element={<Register />}    />

            <Route element={<ProtectedRoute />}>
              <Route path="/watchlist" element={<Watchlist />} />
              <Route path="/profile"   element={<Profile />}   />
            </Route>

            <Route element={<ProtectedRoute requiredRole="ROLE_ADMIN" />}>
              <Route path="/admin" element={<Admin />} />
            </Route>
          </Routes>
        </main>
        <Footer />
      </AuthProvider>
    </BrowserRouter>
  );
}
