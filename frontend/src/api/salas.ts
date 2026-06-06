import { api } from './client';
import type {
  AtualizarSalaRequest,
  AtualizarStatusSalaRequest,
  AtribuirRegraSalaRequest,
  ConsultaDisponibilidadeResponse,
  CriarRegraDisponibilidadeRequest,
  CriarSalaRequest,
  RegraDisponibilidadeResponse,
  SalaResponse,
} from '../types';

export interface ExcecaoDisponibilidadeRequest {
  data: string;
  motivo?: string;
}

export const salasApi = {
  listar: () => api.get<SalaResponse[]>('/salas'),
  obter: (id: string) => api.get<SalaResponse>(`/salas/${id}`),
  criar: (data: CriarSalaRequest) => api.post<SalaResponse>('/salas', data),
  atualizar: (id: string, data: AtualizarSalaRequest) =>
    api.put<SalaResponse>(`/salas/${id}`, data),
  inativar: (id: string) => api.patch<SalaResponse>(`/salas/${id}/inativar`),
  atualizarStatus: (id: string, data: AtualizarStatusSalaRequest) =>
    api.patch<SalaResponse>(`/salas/${id}/status`, data),
  criarRegraDisponibilidade: (salaId: string, data: CriarRegraDisponibilidadeRequest) =>
    api.post<RegraDisponibilidadeResponse>(`/salas/${salaId}/regras-disponibilidade`, data),
  listarRegrasDisponibilidade: (salaId: string) =>
    api.get<RegraDisponibilidadeResponse>(`/salas/${salaId}/regras-disponibilidade`),
  atribuirRegra: (salaId: string, data: AtribuirRegraSalaRequest) =>
    api.put<RegraDisponibilidadeResponse>(`/salas/${salaId}/regra-disponibilidade`, data),
  desatribuirRegra: (salaId: string) =>
    api.delete<void>(`/salas/${salaId}/regra-disponibilidade`),
  consultarDisponibilidade: (salaId: string, data: string) =>
    api.get<ConsultaDisponibilidadeResponse>(`/salas/${salaId}/disponibilidade?data=${data}`),
  criarExcecao: (salaId: string, data: ExcecaoDisponibilidadeRequest) =>
    api.post<void>(`/salas/${salaId}/excecoes-disponibilidade`, data),
};
