import { apiRequest } from "./api";
import type { NotificationItem } from "../types/officehub";

export async function fetchNotifications(): Promise<NotificationItem[]> {
  return apiRequest<NotificationItem[]>("/notifications");
}

export async function markNotificationAsRead(id: number): Promise<void> {
  await apiRequest<void>(`/notifications/${id}/read`, {
    method: "PATCH",
  });
}

export async function markAllNotificationsAsRead(): Promise<void> {
  await apiRequest<void>("/notifications/read-all", {
    method: "PATCH",
  });
}
