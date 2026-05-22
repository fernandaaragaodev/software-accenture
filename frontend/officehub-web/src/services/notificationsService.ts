import { apiRequest } from "./api";
import type { NotificationItem, SessionUser } from "../types/officehub";

function viewerQuery(user: SessionUser): string {
  const q = new URLSearchParams({
    viewerName: user.name,
    viewerRole: user.role,
  });
  return `?${q.toString()}`;
}

export async function fetchNotifications(user: SessionUser): Promise<NotificationItem[]> {
  return apiRequest<NotificationItem[]>(`/notifications${viewerQuery(user)}`);
}

export async function markNotificationAsRead(id: number, user: SessionUser): Promise<void> {
  await apiRequest<void>(`/notifications/${id}/read${viewerQuery(user)}`, {
    method: "PATCH",
  });
}

export async function markAllNotificationsAsRead(user: SessionUser): Promise<void> {
  await apiRequest<void>(`/notifications/read-all${viewerQuery(user)}`, {
    method: "PATCH",
  });
}
