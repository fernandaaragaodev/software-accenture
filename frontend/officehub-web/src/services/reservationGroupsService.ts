import { apiRequest } from "./api";
import type { ReservationStatus, SessionUser } from "../types/officehub";

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

function reservationScopeQuery(user?: Pick<SessionUser, "name" | "role">): string {
  if (!user) return "";
  const params = new URLSearchParams({
    requesterName: user.name,
    requesterRole: user.role,
  });
  return `?${params.toString()}`;
}

export async function fetchReservationGroups(
  user?: Pick<SessionUser, "name" | "role">,
): Promise<ReservationGroup[]> {
  return apiRequest<ReservationGroup[]>(
    `/reservations/groups${reservationScopeQuery(user)}`,
  );
}

export async function fetchReservationGroup(
  groupId: string,
  user?: Pick<SessionUser, "name" | "role">,
): Promise<ReservationGroup> {
  return apiRequest<ReservationGroup>(
    `/reservations/groups/${groupId}${reservationScopeQuery(user)}`,
  );
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

