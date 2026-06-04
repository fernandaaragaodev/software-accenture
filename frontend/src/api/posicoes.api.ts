import { api } from './axios';
import type {
  AtualizarCoordenadasRequest,
  CriarPosicaoRequest,
  Posicao,
  PosicaoEquipamento,
  VincularEquipamentoRequest,
} from '../types/posicao.types';

export const posicoesApi = {
  listarPorSala: (salaId: string) =>
    api.get<Posicao[]>(`/salas/${salaId}/posicoes`),
  buscar: (id: string) => api.get<Posicao>(`/posicoes/${id}`),
  criar: (payload: CriarPosicaoRequest) => api.post<Posicao>('/posicoes', payload),
  atualizarCoordenadas: (id: string, payload: AtualizarCoordenadasRequest) =>
    api.patch<Posicao>(`/posicoes/${id}/coordenadas`, payload),
  inativar: (id: string) => api.patch<Posicao>(`/posicoes/${id}/inativar`),
  vincularEquipamento: (id: string, payload: VincularEquipamentoRequest) =>
    api.post<PosicaoEquipamento>(`/posicoes/${id}/equipamentos`, payload),
  listarEquipamentos: (id: string) =>
    api.get<PosicaoEquipamento[]>(`/posicoes/${id}/equipamentos`),
};
