import { api } from './client';
import type {
  AtualizarCoordenadasPosicaoRequest,
  CriarPosicaoRequest,
  PosicaoResponse,
} from '../types';

export const posicoesApi = {
  listarPorSala: (salaId: string) => api.get<PosicaoResponse[]>(`/salas/${salaId}/posicoes`),
  obter: (id: string) => api.get<PosicaoResponse>(`/posicoes/${id}`),
  criar: (data: CriarPosicaoRequest) => api.post<PosicaoResponse>('/posicoes', data),
  atualizarCoordenadas: (id: string, data: AtualizarCoordenadasPosicaoRequest) =>
    api.patch<PosicaoResponse>(`/posicoes/${id}/coordenadas`, data),
  inativar: (id: string) => api.patch<PosicaoResponse>(`/posicoes/${id}/inativar`),
  reativar: (id: string) => api.patch<PosicaoResponse>(`/posicoes/${id}/reativar`),
};
