import type { JwtPayload, Role } from '../types';

const ACCESS_TOKEN_KEY = 'officehub_access_token';
const REFRESH_TOKEN_KEY = 'officehub_refresh_token';

const LEGACY_ACCESS_TOKEN_KEYS = ['accessToken', 'sgsp_accessToken', 'sgsp_token', 'token'];
const LEGACY_REFRESH_TOKEN_KEYS = ['refreshToken', 'sgsp_refreshToken'];

function readFirst(keys: string[]): string | null {
  for (const key of keys) {
    const value = localStorage.getItem(key);
    if (value && value !== 'undefined' && value !== 'null') return value;
  }
  return null;
}

export function getAccessToken(): string | null {
  return readFirst([ACCESS_TOKEN_KEY, ...LEGACY_ACCESS_TOKEN_KEYS]);
}

export function getRefreshToken(): string | null {
  return readFirst([REFRESH_TOKEN_KEY, ...LEGACY_REFRESH_TOKEN_KEYS]);
}

export function setTokens(accessToken: string, refreshToken: string): void {
  clearTokens();

  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);

  // Compatibilidade com arquivos antigos do projeto que ainda possam ler estas chaves.
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
}

export function clearTokens(): void {
  [
    ACCESS_TOKEN_KEY,
    REFRESH_TOKEN_KEY,
    ...LEGACY_ACCESS_TOKEN_KEYS,
    ...LEGACY_REFRESH_TOKEN_KEYS,
  ].forEach((key) => localStorage.removeItem(key));
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const payload = token.split('.')[1];
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=');
    const decoded = atob(padded);
    return JSON.parse(decoded) as JwtPayload;
  } catch {
    return null;
  }
}

export function getRolesFromToken(): Role[] {
  const token = getAccessToken();
  if (!token) return [];
  const payload = decodeJwt(token);
  return payload?.roles ?? [];
}

export function getEmailFromToken(): string | null {
  const token = getAccessToken();
  if (!token) return null;
  return decodeJwt(token)?.sub ?? null;
}

export function isTokenExpired(): boolean {
  const token = getAccessToken();
  if (!token) return true;
  const payload = decodeJwt(token);
  if (!payload) return true;
  return Date.now() >= payload.exp * 1000;
}

export function hasRole(role: Role, roles?: Role[]): boolean {
  const userRoles = roles ?? getRolesFromToken();
  return userRoles.includes(role);
}

export function hasAnyRole(required: Role[], roles?: Role[]): boolean {
  const userRoles = roles ?? getRolesFromToken();
  return required.some((role) => userRoles.includes(role));
}

export const RESERVATION_IDS_KEY = 'officehub_reserva_ids';

export function getStoredReservationIds(): string[] {
  try {
    const raw = localStorage.getItem(RESERVATION_IDS_KEY);
    return raw ? (JSON.parse(raw) as string[]) : [];
  } catch {
    return [];
  }
}

export function addStoredReservationId(id: string): void {
  const ids = getStoredReservationIds();
  if (!ids.includes(id)) {
    localStorage.setItem(RESERVATION_IDS_KEY, JSON.stringify([id, ...ids]));
  }
}

export function removeStoredReservationId(id: string): void {
  const ids = getStoredReservationIds().filter((stored) => stored !== id);
  localStorage.setItem(RESERVATION_IDS_KEY, JSON.stringify(ids));
}
