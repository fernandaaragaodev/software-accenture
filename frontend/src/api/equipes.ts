import { api } from './client';
import type {
  AdicionarMembroEquipeRequest,
  CriarEquipeRequest,
  EquipeResponse,
  EquipeResumoResponse,
  ValidacaoSenhaRequest,
} from '../types';

export const equipesApi = {
  listar: () => api.get<EquipeResumoResponse[]>('/equipes'),
  listarMinhas: () => api.get<EquipeResumoResponse[]>('/equipes/minhas'),
  obter: (id: string) => api.get<EquipeResponse>(`/equipes/${id}`),
  criar: (data: CriarEquipeRequest) => api.post<EquipeResponse>('/equipes', data),
  adicionarMembro: (id: string, data: AdicionarMembroEquipeRequest) =>
    api.post<EquipeResponse>(`/equipes/${id}/membros`, data),
  removerMembro: (id: string, usuarioId: string, data: ValidacaoSenhaRequest) =>
    api.delete<EquipeResponse>(`/equipes/${id}/membros/${usuarioId}`, data),
  desfazer: (id: string, data: ValidacaoSenhaRequest) =>
    api.delete<void>(`/equipes/${id}`, data),
};
