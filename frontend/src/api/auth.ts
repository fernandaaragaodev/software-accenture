import { api } from './client';
import type {
  CriarUsuarioRequest,
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  UsuarioResponse,
} from '../types';
import { clearTokens, getRefreshToken, setTokens } from '../utils/auth';

export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  clearTokens();
  const response = await api.post<LoginResponse>('/auth/login', credentials);
  setTokens(response.accessToken, response.refreshToken);
  return response;
}

export async function logout(): Promise<void> {
  const refreshToken = getRefreshToken();
  if (refreshToken) {
    try {
      await api.post('/auth/logout', { refreshToken } satisfies RefreshTokenRequest);
    } catch {
      // ignore logout errors
    }
  }
  clearTokens();
}

export async function registerUser(data: CriarUsuarioRequest): Promise<UsuarioResponse> {
  return api.post<UsuarioResponse>('/auth/register', data);
}

export async function getMe(): Promise<UsuarioResponse> {
  return api.get<UsuarioResponse>('/auth/me');
}

export async function healthCheck(): Promise<{ status: string; timestamp: string }> {
  return api.get('/health');
}
