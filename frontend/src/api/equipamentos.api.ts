import { api } from './axios';
import type {
  CriarTipoEquipamentoRequest,
  TipoEquipamento,
} from '../types/equipamento.types';

export const equipamentosApi = {
  listar: () => api.get<TipoEquipamento[]>('/tipos-equipamento'),
  buscar: (id: string) => api.get<TipoEquipamento>(`/tipos-equipamento/${id}`),
  criar: (payload: CriarTipoEquipamentoRequest) =>
    api.post<TipoEquipamento>('/tipos-equipamento', payload),
  atualizar: (id: string, payload: CriarTipoEquipamentoRequest) =>
    api.put<TipoEquipamento>(`/tipos-equipamento/${id}`, payload),
  inativar: (id: string) =>
    api.patch<TipoEquipamento>(`/tipos-equipamento/${id}/inativar`),
};
