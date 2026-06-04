import { api } from './axios';
import type {
  CancelarReservaRequest,
  RejeitarReservaRequest,
  Reserva,
  SolicitarReservaRequest,
} from '../types/reserva.types';

export const reservasApi = {
  solicitar: (payload: SolicitarReservaRequest) =>
    api.post<Reserva>('/reservas', payload),
  buscar: (id: string) => api.get<Reserva>(`/reservas/${id}`),
  confirmar: (id: string) => api.patch<Reserva>(`/reservas/${id}/confirmar`),
  rejeitar: (id: string, payload: RejeitarReservaRequest) =>
    api.patch<Reserva>(`/reservas/${id}/rejeitar`, payload),
  cancelar: (id: string, payload: CancelarReservaRequest) =>
    api.delete<Reserva>(`/reservas/${id}`, { data: payload }),
};
