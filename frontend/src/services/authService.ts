import { authApi } from '../api/auth.api';
import { useAuthStore } from '../stores/authStore';
import type { LoginRequest, LoginResponse } from '../types/auth.types';
import { getApiErrorMessage } from '../utils/apiError';
import { saveTokens, clearTokens, readAccessToken, readRefreshToken } from '../utils/tokenStorage';

/**
 * Fluxo de login compatível com POST /api/v1/auth/login
 * Payload: { email, senha }
 * Resposta: { accessToken, refreshToken, tokenType }
 */
export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  const payload: LoginRequest = {
    email: credentials.email.trim(),
    senha: credentials.senha,
  };

  console.log('Payload enviado:', payload);

  try {
    const response = await authApi.login(payload);
    console.log('Resposta login:', response.data);

    const { accessToken, refreshToken } = response.data;

    if (!accessToken || !refreshToken) {
      throw new Error('Resposta de login incompleta: accessToken ou refreshToken ausente.');
    }

    saveTokens(accessToken, refreshToken);
    useAuthStore.getState().setSession(response.data);

    return response.data;
  } catch (error) {
    const message = getApiErrorMessage(
      error,
      'Não foi possível entrar. Verifique e-mail e senha.',
    );
    throw new Error(message);
  }
}

export async function logout(): Promise<void> {
  const refreshToken = readRefreshToken() ?? useAuthStore.getState().refreshToken;
  const accessToken = readAccessToken() ?? useAuthStore.getState().accessToken;

  try {
    if (refreshToken) {
      await authApi.logout({ refreshToken }, accessToken ?? undefined);
    }
  } catch {
    /* logout remoto opcional */
  } finally {
    clearTokens();
    useAuthStore.getState().clearSession();
  }
}

export function hydrateAuthFromStorage(): void {
  useAuthStore.getState().hydrate();
}
