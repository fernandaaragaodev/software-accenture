import { api } from './client';
import type {
  AceitarSugestaoReservaRequest,
  CancelarReservaRequest,
  RejeitarReservaRequest,
  PageResponse,
  ReservaResumoResponse,
  ReservaResponse,
  SolicitarReservaRequest,
  SugestaoAlocacaoResponse,
  SugestaoOutraAlocacaoRequest,
} from '../types';
import { addStoredReservationId } from '../utils/auth';

export const reservasApi = {
  sugerir: (data: SolicitarReservaRequest) =>
    api.post<SugestaoAlocacaoResponse>('/reservas/sugerir', data),
  sugerirOutra: (data: SugestaoOutraAlocacaoRequest) =>
    api.post<SugestaoAlocacaoResponse>('/reservas/sugerir/outra', data),
  solicitar: async (data: AceitarSugestaoReservaRequest) => {
    const reserva = await api.post<ReservaResponse>('/reservas', data);
    addStoredReservationId(reserva.id);
    return reserva;
  },
  listar: (canceladas = false, data?: string, page = 0, size = 15) => {
    const params = new URLSearchParams({
      canceladas: String(canceladas),
      page: String(page),
      size: String(size),
    });
    if (data) params.set('data', data);
    return api.get<PageResponse<ReservaResumoResponse>>(`/reservas?${params.toString()}`);
  },
  obter: (id: string) => api.get<ReservaResponse>(`/reservas/${id}`),
  confirmar: (id: string) => api.patch<ReservaResponse>(`/reservas/${id}/confirmar`),
  rejeitar: (id: string, data: RejeitarReservaRequest) =>
    api.patch<ReservaResponse>(`/reservas/${id}/rejeitar`, data),
  cancelar: (id: string, data: CancelarReservaRequest) =>
    api.delete<ReservaResponse>(`/reservas/${id}`, data),
};
