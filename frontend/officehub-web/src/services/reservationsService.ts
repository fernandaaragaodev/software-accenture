import { apiRequest } from "./api";
import type { CreateReservationPayload, Reservation } from "../types/officehub";

export async function fetchReservations(): Promise<Reservation[]> {
  return apiRequest<Reservation[]>("/reservations");
}

export async function createReservation(
  payload: CreateReservationPayload,
): Promise<Reservation> {
  return apiRequest<Reservation>("/reservations", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function cancelReservation(id: number): Promise<void> {
  await apiRequest<void>(`/reservations/${id}`, {
    method: "DELETE",
  });
}

