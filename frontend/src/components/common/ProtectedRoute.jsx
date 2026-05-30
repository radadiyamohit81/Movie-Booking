import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

/**
 * Protects routes that require authentication.
 * requiredRole prop enforces role-based access (e.g. ROLE_ADMIN).
 *
 * WHY <Outlet />?
 *   React Router v6 nested routes: ProtectedRoute wraps child routes.
 *   If auth passes, <Outlet /> renders the matched child route.
 *   Otherwise, <Navigate> redirects without rendering anything.
 */
export default function ProtectedRoute({ requiredRole }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
