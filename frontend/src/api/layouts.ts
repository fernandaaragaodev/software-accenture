import { api } from './client';
import type {
  AtualizarCoordenadasPosicaoRequest,
  CriarLayoutRequest,
  LayoutResponse,
} from '../types';

export const layoutsApi = {
  criar: (data: CriarLayoutRequest) => api.post<LayoutResponse>('/layouts', data),
  obterAtivo: (salaId: string) => api.get<LayoutResponse>(`/salas/${salaId}/layout/ativo`),
  aprovar: (id: string) => api.patch<LayoutResponse>(`/layouts/${id}/aprovar`),
  atualizarCoordenadasPosicao: (posicaoId: string, data: AtualizarCoordenadasPosicaoRequest) =>
    api.patch<void>(`/layouts/posicoes/${posicaoId}/coordenadas`, data),
};
