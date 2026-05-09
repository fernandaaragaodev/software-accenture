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

export async function createReservationsBatch(
  payloads: CreateReservationPayload[],
): Promise<Reservation[]> {
  return apiRequest<Reservation[]>("/reservations/batch", {
    method: "POST",
    body: JSON.stringify({ reservations: payloads }),
  });
}

export async function cancelReservation(
  id: number,
  requesterName: string,
  requesterRole: string,
): Promise<void> {
  const q = new URLSearchParams({ requesterName, requesterRole });
  await apiRequest<void>(`/reservations/${id}?${q.toString()}`, {
    method: "DELETE",
  });
}

