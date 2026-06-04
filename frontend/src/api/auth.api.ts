import axios from 'axios';
import { API_BASE_URL } from '../config/api';
import type {
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RegisterRequest,
  UsuarioResponse,
} from '../types/auth.types';
import { readAccessToken } from '../utils/tokenStorage';

const publicApi = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

export const authApi = {
  /**
   * POST {baseURL}/auth/login
   * → http://localhost:8080/api/v1/auth/login (com VITE_API_URL padrão)
   */
  login: (payload: LoginRequest) =>
    publicApi.post<LoginResponse>('/auth/login', {
      email: payload.email,
      senha: payload.senha,
    }),

  refresh: (payload: RefreshTokenRequest) =>
    publicApi.post<LoginResponse>('/auth/refresh', {
      refreshToken: payload.refreshToken,
    }),

  logout: (payload: RefreshTokenRequest, accessToken?: string) =>
    axios.post<void>(`${API_BASE_URL}/auth/logout`, { refreshToken: payload.refreshToken }, {
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
    }),

  register: (payload: RegisterRequest) => {
    const token = readAccessToken();
    return axios.post<UsuarioResponse>(`${API_BASE_URL}/auth/register`, payload, {
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        'Content-Type': 'application/json',
      },
    });
  },
};
