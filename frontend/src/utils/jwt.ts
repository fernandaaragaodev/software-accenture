import type { Role } from '../types/auth.types';

interface JwtPayload {
  sub?: string;
  roles?: string[];
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const part = token.split('.')[1];
    if (!part) return null;
    const json = atob(part.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

export function extractRolesFromToken(token: string): Role[] {
  const payload = decodeJwt(token);
  if (!payload?.roles) return [];
  return payload.roles.filter((r): r is Role =>
    ['ADMIN_SALA', 'GESTOR_RESERVAS', 'USUARIO_FINAL', 'INTEGRADOR'].includes(r),
  );
}

export function extractEmailFromToken(token: string): string | null {
  return decodeJwt(token)?.sub ?? null;
}
