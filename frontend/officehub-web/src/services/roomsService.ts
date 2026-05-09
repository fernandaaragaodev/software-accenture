import { apiRequest } from "./api";
import type { Room } from "../types/officehub";

export async function fetchRooms(): Promise<Room[]> {
  return apiRequest<Room[]>("/rooms");
}

