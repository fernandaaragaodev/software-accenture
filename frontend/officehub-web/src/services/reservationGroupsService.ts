import { apiRequest } from "./api";
import type { ReservationStatus } from "../types/officehub";

export interface ReservationGroupMember {
  reservationId: number;
  user: string;
  seatCode: string;
  seatType: string;
  requestedEquipment: string[];
  status: ReservationStatus;
}

export interface ReservationGroup {
  groupId: string;
  isBatch: boolean;
  room: string;
  requesterName: string;
  requesterRole: string;
  date: string;
  start: string;
  end: string;
  status: ReservationStatus;
  peopleCount: number;
  members: ReservationGroupMember[];
}

export async function fetchReservationGroups(): Promise<ReservationGroup[]> {
  return apiRequest<ReservationGroup[]>("/reservations/groups");
}

export async function fetchReservationGroup(groupId: string): Promise<ReservationGroup> {
  return apiRequest<ReservationGroup>(`/reservations/groups/${groupId}`);
}

export async function cancelReservationGroup(
  groupId: string,
  requesterName: string,
  requesterRole: string,
): Promise<void> {
  const q = new URLSearchParams({ requesterName, requesterRole });
  await apiRequest<void>(`/reservations/groups/${groupId}?${q.toString()}`, {
    method: "DELETE",
  });
}

