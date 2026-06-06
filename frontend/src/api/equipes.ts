import { api } from './client';
import type {
  AdicionarMembroEquipeRequest,
  CriarEquipeRequest,
  EquipeResponse,
  EquipeResumoResponse,
} from '../types';

export const equipesApi = {
  listar: () => api.get<EquipeResumoResponse[]>('/equipes'),
  obter: (id: string) => api.get<EquipeResponse>(`/equipes/${id}`),
  criar: (data: CriarEquipeRequest) => api.post<EquipeResponse>('/equipes', data),
  adicionarMembro: (id: string, data: AdicionarMembroEquipeRequest) =>
    api.post<EquipeResponse>(`/equipes/${id}/membros`, data),
  removerMembro: (id: string, usuarioId: string) =>
    api.delete<EquipeResponse>(`/equipes/${id}/membros/${usuarioId}`),
  desmembrar: (id: string) => api.delete<void>(`/equipes/${id}`),
};
