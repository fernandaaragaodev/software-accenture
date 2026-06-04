import { api } from './axios';
import type {
  AtualizarStatusSalaRequest,
  CriarSalaRequest,
  Sala,
} from '../types/sala.types';

export const salasApi = {
  listar: () => api.get<Sala[]>('/salas'),
  buscar: (id: string) => api.get<Sala>(`/salas/${id}`),
  criar: (payload: CriarSalaRequest) => api.post<Sala>('/salas', payload),
  atualizar: (id: string, payload: CriarSalaRequest) =>
    api.put<Sala>(`/salas/${id}`, payload),
  inativar: (id: string) => api.patch<Sala>(`/salas/${id}/inativar`),
  atualizarStatus: (id: string, payload: AtualizarStatusSalaRequest) =>
    api.patch<Sala>(`/salas/${id}/status`, payload),
};
