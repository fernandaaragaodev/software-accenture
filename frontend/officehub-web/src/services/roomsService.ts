import { apiRequest } from "./api";
import type { Room, RoomPosition } from "../types/officehub";

export async function fetchRooms(): Promise<Room[]> {
  return apiRequest<Room[]>("/rooms");
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

