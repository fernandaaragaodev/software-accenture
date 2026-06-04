import type { Role } from '../types/auth.types';

export const ROLES = {
  ADMIN_SALA: 'ADMIN_SALA' as Role,
  GESTOR_RESERVAS: 'GESTOR_RESERVAS' as Role,
  USUARIO_FINAL: 'USUARIO_FINAL' as Role,
  INTEGRADOR: 'INTEGRADOR' as Role,
};

export function hasRole(roles: Role[], role: Role): boolean {
  return roles.includes(role);
}

export function hasAnyRole(roles: Role[], allowed: Role[]): boolean {
  return allowed.some((r) => roles.includes(r));
}

export function canManageSalas(roles: Role[]): boolean {
  return hasRole(roles, ROLES.ADMIN_SALA);
}

export function canManageReservasGestor(roles: Role[]): boolean {
  return hasRole(roles, ROLES.GESTOR_RESERVAS);
}

export function canCreateReserva(roles: Role[]): boolean {
  return hasAnyRole(roles, [
    ROLES.USUARIO_FINAL,
    ROLES.INTEGRADOR,
    ROLES.GESTOR_RESERVAS,
  ]);
}

export function canCancelReserva(roles: Role[]): boolean {
  return hasAnyRole(roles, [ROLES.USUARIO_FINAL, ROLES.GESTOR_RESERVAS]);
}

export function canViewReservaDetalheApi(roles: Role[]): boolean {
  return hasRole(roles, ROLES.GESTOR_RESERVAS);
}

export function canConsultarDisponibilidade(roles: Role[]): boolean {
  return hasAnyRole(roles, [
    ROLES.USUARIO_FINAL,
    ROLES.INTEGRADOR,
    ROLES.GESTOR_RESERVAS,
    ROLES.ADMIN_SALA,
  ]);
}

export function canViewNotificacoes(roles: Role[]): boolean {
  return hasRole(roles, ROLES.GESTOR_RESERVAS);
}

/** Tokens: ver `config/api.ts` → AUTH_STORAGE_KEYS (`accessToken`, `refreshToken`) */
export const STORAGE_KEYS = {
  USUARIO_ID: 'sgsp_usuario_id',
  RESERVA_IDS: 'sgsp_reserva_ids',
} as const;

export function getStoredReservaIds(): string[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.RESERVA_IDS);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as unknown;
    return Array.isArray(parsed) ? parsed.filter((id) => typeof id === 'string') : [];
  } catch {
    return [];
  }
}

export function addStoredReservaId(id: string): void {
  const ids = getStoredReservaIds();
  if (!ids.includes(id)) {
    localStorage.setItem(
      STORAGE_KEYS.RESERVA_IDS,
      JSON.stringify([id, ...ids].slice(0, 50)),
    );
  }
}
