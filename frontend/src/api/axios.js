import axios from 'axios';

/**
 * Pre-configured Axios instance.
 *
 * WHY a shared instance?
 *   - baseURL set once — change the API host in one place.
 *   - Request interceptor attaches JWT to every call automatically.
 *   - Response interceptor handles global 401 (token expired → logout).
 *
 * WHY not use the CRA proxy?
 *   The proxy only works in development. Using the full URL here makes the
 *   config explicit and works identically in production builds.
 */
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// ── Request interceptor: attach JWT ──────────────────────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor: global 401 handling ─────────────────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid — clear storage and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
