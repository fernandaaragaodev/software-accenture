import { api } from './client';
import type {
  PosicaoEquipamentoResponse,
  TipoEquipamentoRequest,
  TipoEquipamentoResponse,
  VincularEquipamentoPosicaoRequest,
} from '../types';

export const tiposEquipamentoApi = {
  listar: () => api.get<TipoEquipamentoResponse[]>('/tipos-equipamento'),
  obter: (id: string) => api.get<TipoEquipamentoResponse>(`/tipos-equipamento/${id}`),
  criar: (data: TipoEquipamentoRequest) =>
    api.post<TipoEquipamentoResponse>('/tipos-equipamento', data),
  atualizar: (id: string, data: TipoEquipamentoRequest) =>
    api.put<TipoEquipamentoResponse>(`/tipos-equipamento/${id}`, data),
  inativar: (id: string) => api.patch<TipoEquipamentoResponse>(`/tipos-equipamento/${id}/inativar`),
  ativar: (id: string) => api.patch<TipoEquipamentoResponse>(`/tipos-equipamento/${id}/ativar`),
  listarPorPosicao: (posicaoId: string) =>
    api.get<PosicaoEquipamentoResponse[]>(`/posicoes/${posicaoId}/equipamentos`),
  vincularPosicao: (posicaoId: string, data: VincularEquipamentoPosicaoRequest) =>
    api.post<PosicaoEquipamentoResponse>(`/posicoes/${posicaoId}/equipamentos`, data),
};
