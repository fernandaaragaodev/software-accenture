import { apiRequest } from "./api";
import type { Room, RoomPosition } from "../types/officehub";

export interface AvailablePositionType {
  type: string;
  availableCount: number;
}

export interface AvailableRoomForReservation extends Room {
  matchingAvailablePositions: number;
}

export async function fetchRooms(): Promise<Room[]> {
  return apiRequest<Room[]>("/rooms");
}

export async function fetchAvailablePositionTypes(
  date: string,
  start: string,
  end: string,
): Promise<AvailablePositionType[]> {
  const params = new URLSearchParams({ date, start, end });
  return apiRequest<AvailablePositionType[]>(
    `/rooms/available-position-types?${params.toString()}`,
  );
}

export async function fetchAvailableRoomsForReservation(
  date: string,
  start: string,
  end: string,
  seatType: string,
): Promise<AvailableRoomForReservation[]> {
  const params = new URLSearchParams({ date, start, end, seatType });
  return apiRequest<AvailableRoomForReservation[]>(
    `/rooms/available?${params.toString()}`,
  );
}

export async function fetchRoomPositions(
  roomId: number,
  date: string,
  start: string,
  end: string,
): Promise<RoomPosition[]> {
  const params = new URLSearchParams({ date, start, end });
  return apiRequest<RoomPosition[]>(`/rooms/${roomId}/positions?${params.toString()}`);
}

export async function fetchRoomPositionsOverview(
  roomId: number,
): Promise<RoomPosition[]> {
  return apiRequest<RoomPosition[]>(`/rooms/${roomId}/positions/overview`);
}

export async function setPositionBlocked(
  roomId: number,
  positionCode: string,
  blocked: boolean,
  requesterRole: string,
): Promise<void> {
  const q = new URLSearchParams({ requesterRole });
  const action = blocked ? "block" : "unblock";
  const encodedCode = encodeURIComponent(positionCode);
  await apiRequest<void>(
    `/rooms/${roomId}/positions/${encodedCode}/${action}?${q.toString()}`,
    { method: "POST" },
  );
}

export async function setRoomBlocked(
  roomId: number,
  blocked: boolean,
  requesterRole: string,
  adminPassword?: string,
): Promise<void> {
  const q = new URLSearchParams({ requesterRole });
  if (blocked) {
    await apiRequest<void>(`/rooms/${roomId}/block?${q.toString()}`, {
      method: "POST",
      body: JSON.stringify({ adminPassword: adminPassword ?? "" }),
    });
    return;
  }
  await apiRequest<void>(`/rooms/${roomId}/unblock?${q.toString()}`, {
    method: "POST",
  });
}

