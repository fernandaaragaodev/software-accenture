import { api } from './axios';
import type {
  CriarLayoutRequest,
  CriarRegraDisponibilidadeRequest,
  ExcecaoDisponibilidadeRequest,
  Layout,
  RegraDisponibilidade,
  ValidacaoDisponibilidade,
} from '../types/disponibilidade.types';
import type { AtualizarCoordenadasRequest } from '../types/posicao.types';

export const disponibilidadeApi = {
  criarRegra: (salaId: string, payload: CriarRegraDisponibilidadeRequest) =>
    api.post<RegraDisponibilidade>(`/salas/${salaId}/regras-disponibilidade`, payload),
  buscarRegra: (salaId: string) =>
    api.get<RegraDisponibilidade>(`/salas/${salaId}/regras-disponibilidade`),
  consultar: (salaId: string, data: string) =>
    api.get<ValidacaoDisponibilidade>(`/salas/${salaId}/disponibilidade`, {
      params: { data },
    }),
  adicionarExcecao: (salaId: string, payload: ExcecaoDisponibilidadeRequest) =>
    api.post<void>(`/salas/${salaId}/excecoes-disponibilidade`, payload),
};

export const layoutsApi = {
  criar: (payload: CriarLayoutRequest) => api.post<Layout>('/layouts', payload),
  buscarAtivo: (salaId: string) =>
    api.get<Layout>(`/salas/${salaId}/layout/ativo`),
  aprovar: (id: string) => api.patch<Layout>(`/layouts/${id}/aprovar`),
  ajustarCoordenadasPosicao: (
    posicaoId: string,
    payload: AtualizarCoordenadasRequest,
  ) => api.patch<void>(`/layouts/posicoes/${posicaoId}/coordenadas`, payload),
};
