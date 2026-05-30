import React, { createContext, useContext, useState, useCallback } from 'react';
import { login as loginApi, register as registerApi } from '../api/authApi';

/**
 * Auth context — source of truth for authentication state across the app.
 *
 * Why Context API (not Redux)?
 *   Auth state is global but simple: {token, user}. Context API handles this
 *   with zero boilerplate. Redux adds value for complex shared state with many
 *   reducers — overkill here.
 *
 * Storage strategy:
 *   token and user are persisted to localStorage so they survive page refresh.
 *   On mount, the state is initialised from localStorage (see initial state).
 *
 * Security note:
 *   localStorage is accessible to JavaScript running on the same origin, making
 *   it vulnerable to XSS. For higher security, store the token in an httpOnly
 *   cookie (requires backend support). For this project localStorage is
 *   acceptable given Content-Security-Policy headers would mitigate XSS.
 */
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [user,  setUser]  = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback(async (credentials) => {
    const { data } = await loginApi(credentials);
    localStorage.setItem('token', data.token);
    localStorage.setItem('user',  JSON.stringify({ username: data.username, role: data.role }));
    setToken(data.token);
    setUser({ username: data.username, role: data.role });
    return data;
  }, []);

  const register = useCallback(async (credentials) => {
    const { data } = await registerApi(credentials);
    localStorage.setItem('token', data.token);
    localStorage.setItem('user',  JSON.stringify({ username: data.username, role: data.role }));
    setToken(data.token);
    setUser({ username: data.username, role: data.role });
    return data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  const isAuthenticated = Boolean(token);
  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider value={{ token, user, isAuthenticated, isAdmin, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
