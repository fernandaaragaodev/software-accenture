import { api } from './axios';

export const healthApi = {
  check: () => api.get<Record<string, unknown>>('/health'),
};
