import axios from 'axios';

const getBaseUrl = (): string => {
  const envUrl = import.meta.env.VITE_API_BASE_URL;
  if (!envUrl) return '/api';
  
  let url = envUrl.trim();
  // If Render passed an internal service name (e.g. codereview-backend-usj9) without a domain
  if (!url.includes('.') && !url.startsWith('/')) {
    url = `${url}.onrender.com`;
  }
  if (!url.startsWith('http://') && !url.startsWith('https://') && !url.startsWith('/')) {
    url = `https://${url}`;
  }
  if (url.startsWith('http') && !url.endsWith('/api')) {
    url = `${url.replace(/\/+$/, '')}/api`;
  }
  return url;
};

const api = axios.create({
  baseURL: getBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Don't auto-redirect on login or signup failures
      if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/signup')) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
