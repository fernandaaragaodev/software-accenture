import type { JwtPayload, Role } from '../types';

const ACCESS_TOKEN_KEY = 'officehub_access_token';
const REFRESH_TOKEN_KEY = 'officehub_refresh_token';

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setTokens(accessToken: string, refreshToken: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const payload = token.split('.')[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
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
