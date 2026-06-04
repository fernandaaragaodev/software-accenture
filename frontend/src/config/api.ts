/**
 * URL raiz do backend (sem /api/v1).
 * Ex.: http://localhost:8080
 *
 * Em dev, deixe vazio para usar o proxy do Vite (`/api/v1` → localhost:8080)
 * e evitar bloqueio de CORS no navegador.
 */
const API_ROOT = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(
  /\/$/,
  '',
);

/** Base exata dos endpoints: http://localhost:8080/api/v1 */
export const API_BASE_URL = API_ROOT ? `${API_ROOT}/api/v1` : '/api/v1';

export const AUTH_ENDPOINTS = {
  login: `${API_BASE_URL}/auth/login`,
  refresh: `${API_BASE_URL}/auth/refresh`,
  logout: `${API_BASE_URL}/auth/logout`,
  register: `${API_BASE_URL}/auth/register`,
} as const;

/** Chaves de localStorage alinhadas ao contrato do backend/Swagger */
export const AUTH_STORAGE_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
} as const;
