import { api } from './client';
import type { DashboardStatsResponse } from '../types';

export const relatoriosApi = {
  obterDashboard: () => api.get<DashboardStatsResponse>('/relatorios/dashboard'),
};
