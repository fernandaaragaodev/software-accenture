import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL } from '../config/api';
import { authApi } from './auth.api';
import { useAuthStore } from '../stores/authStore';
import { readAccessToken, readRefreshToken, saveTokens, clearTokens } from '../utils/tokenStorage';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

let isRefreshing = false;
let refreshQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

function processQueue(error: unknown, token: string | null) {
  refreshQueue.forEach((p) => {
    if (error) p.reject(error);
    else if (token) p.resolve(token);
  });
  refreshQueue = [];
}

function resolveAccessToken(): string | null {
  return useAuthStore.getState().accessToken ?? readAccessToken();
}

function resolveRefreshToken(): string | null {
  return useAuthStore.getState().refreshToken ?? readRefreshToken();
}

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = resolveAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const status = error.response?.status;

    if (
      status !== 401 ||
      !original ||
      original._retry ||
      original.url?.includes('/auth/login') ||
      original.url?.includes('/auth/refresh')
    ) {
      return Promise.reject(error);
    }

    const refreshToken = resolveRefreshToken();
    if (!refreshToken) {
      clearTokens();
      useAuthStore.getState().clearSession();
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        refreshQueue.push({
          resolve: (token: string) => {
            original.headers.Authorization = `Bearer ${token}`;
            resolve(api(original));
          },
          reject,
        });
      });
    }

    original._retry = true;
    isRefreshing = true;

    try {
      const { data } = await authApi.refresh({ refreshToken });
      saveTokens(data.accessToken, data.refreshToken);
      useAuthStore.getState().setSession(data);
      processQueue(null, data.accessToken);
      original.headers.Authorization = `Bearer ${data.accessToken}`;
      return api(original);
    } catch (refreshError) {
      processQueue(refreshError, null);
      clearTokens();
      useAuthStore.getState().clearSession();
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);
