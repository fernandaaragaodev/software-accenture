import { AUTH_STORAGE_KEYS } from '../config/api';

const LEGACY_ACCESS = 'sgsp_access_token';
const LEGACY_REFRESH = 'sgsp_refresh_token';

export function saveTokens(accessToken: string, refreshToken: string): void {
  localStorage.setItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN, accessToken);
  localStorage.setItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
  localStorage.removeItem(LEGACY_ACCESS);
  localStorage.removeItem(LEGACY_REFRESH);
}

export function readAccessToken(): string | null {
  return (
    localStorage.getItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN) ??
    localStorage.getItem(LEGACY_ACCESS)
  );
}

export function readRefreshToken(): string | null {
  return (
    localStorage.getItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN) ??
    localStorage.getItem(LEGACY_REFRESH)
  );
}

export function clearTokens(): void {
  localStorage.removeItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN);
  localStorage.removeItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN);
  localStorage.removeItem(LEGACY_ACCESS);
  localStorage.removeItem(LEGACY_REFRESH);
}
