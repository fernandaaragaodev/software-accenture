import { create } from 'zustand';
import type { AuthUser, LoginResponse, Role } from '../types/auth.types';
import { extractEmailFromToken, extractRolesFromToken } from '../utils/jwt';
import {
  clearTokens,
  readAccessToken,
  readRefreshToken,
  saveTokens,
} from '../utils/tokenStorage';
import { STORAGE_KEYS } from '../utils/permissions';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: AuthUser | null;
  isAuthenticated: boolean;
  setSession: (response: LoginResponse, usuarioId?: string | null) => void;
  setUsuarioId: (id: string) => void;
  clearSession: () => void;
  hydrate: () => void;
}

function buildUser(accessToken: string, usuarioId: string | null): AuthUser {
  return {
    email: extractEmailFromToken(accessToken) ?? '',
    roles: extractRolesFromToken(accessToken),
    usuarioId,
  };
}

function readStoredUsuarioId(): string | null {
  return localStorage.getItem(STORAGE_KEYS.USUARIO_ID);
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  refreshToken: null,
  user: null,
  isAuthenticated: false,

  setSession: (response, usuarioId) => {
    const uid = usuarioId ?? readStoredUsuarioId();

    saveTokens(response.accessToken, response.refreshToken);
    if (uid) localStorage.setItem(STORAGE_KEYS.USUARIO_ID, uid);

    set({
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      user: buildUser(response.accessToken, uid),
      isAuthenticated: true,
    });
  },

  setUsuarioId: (id) => {
    localStorage.setItem(STORAGE_KEYS.USUARIO_ID, id);
    set((state) => ({
      user: state.user
        ? { ...state.user, usuarioId: id }
        : state.accessToken
          ? buildUser(state.accessToken, id)
          : null,
    }));
  },

  clearSession: () => {
    clearTokens();
    set({
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
    });
  },

  hydrate: () => {
    const accessToken = readAccessToken();
    const refreshToken = readRefreshToken();
    if (!accessToken || !refreshToken) return;

    const usuarioId = readStoredUsuarioId();
    set({
      accessToken,
      refreshToken,
      user: buildUser(accessToken, usuarioId),
      isAuthenticated: true,
    });
  },
}));

export function getAccessToken(): string | null {
  return useAuthStore.getState().accessToken ?? readAccessToken();
}

export function getRefreshToken(): string | null {
  return useAuthStore.getState().refreshToken ?? readRefreshToken();
}

export function getUserRoles(): Role[] {
  return useAuthStore.getState().user?.roles ?? [];
}
