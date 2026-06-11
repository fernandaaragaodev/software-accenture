import { api } from './client';
import type {
  CancelarReservaRequest,
  RejeitarReservaRequest,
  ReservaResumoResponse,
  ReservaResponse,
  SolicitarReservaRequest,
} from '../types';
import { addStoredReservationId } from '../utils/auth';

export const reservasApi = {
  solicitar: async (data: SolicitarReservaRequest) => {
    const reserva = await api.post<ReservaResponse>('/reservas', data);
    addStoredReservationId(reserva.id);
    return reserva;
  },
  listar: (canceladas = false, data?: string) => {
    const params = new URLSearchParams({ canceladas: String(canceladas) });
    if (data) params.set('data', data);
    return api.get<ReservaResumoResponse[]>(`/reservas?${params.toString()}`);
  },
  obter: (id: string) => api.get<ReservaResponse>(`/reservas/${id}`),
  confirmar: (id: string) => api.patch<ReservaResponse>(`/reservas/${id}/confirmar`),
  rejeitar: (id: string, data: RejeitarReservaRequest) =>
    api.patch<ReservaResponse>(`/reservas/${id}/rejeitar`, data),
  cancelar: (id: string, data: CancelarReservaRequest) =>
    api.delete<ReservaResponse>(`/reservas/${id}`, data),
};
