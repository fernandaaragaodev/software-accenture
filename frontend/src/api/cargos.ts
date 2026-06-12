import { api } from './client';
import type { CargoResponse } from '../types';

export const cargosApi = {
  listar: () => api.get<CargoResponse[]>('/cargos'),
};
