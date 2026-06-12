import { api } from './client';
import type { EspecialidadeResponse } from '../types';

export const especialidadesApi = {
  listar: () => api.get<EspecialidadeResponse[]>('/especialidades'),
};
