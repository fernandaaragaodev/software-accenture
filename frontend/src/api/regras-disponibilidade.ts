import { api } from './client';
import type {
  AtribuirRegraSalaRequest,
  CriarRegraDisponibilidadeIndependenteRequest,
  RegraDisponibilidadeResponse,
} from '../types';

export const regrasDisponibilidadeApi = {
  listar: () => api.get<RegraDisponibilidadeResponse[]>('/regras-disponibilidade'),
  obter: (id: string) => api.get<RegraDisponibilidadeResponse>(`/regras-disponibilidade/${id}`),
  criar: (data: CriarRegraDisponibilidadeIndependenteRequest) =>
    api.post<RegraDisponibilidadeResponse>('/regras-disponibilidade', data),
  atualizar: (id: string, data: CriarRegraDisponibilidadeIndependenteRequest) =>
    api.put<RegraDisponibilidadeResponse>(`/regras-disponibilidade/${id}`, data),
  excluir: (id: string) => api.delete<void>(`/regras-disponibilidade/${id}`),
  atribuirSala: (salaId: string, data: AtribuirRegraSalaRequest) =>
    api.put<RegraDisponibilidadeResponse>(`/salas/${salaId}/regra-disponibilidade`, data),
  desatribuirSala: (salaId: string) =>
    api.delete<void>(`/salas/${salaId}/regra-disponibilidade`),
};
