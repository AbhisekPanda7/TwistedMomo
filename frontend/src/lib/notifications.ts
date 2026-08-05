import { api } from "./api";
import type { ApiPageResponse } from "./catalog";

export type Notification = {
  id: number;
  type: string;
  title: string;
  body: string;
  orderId: number | null;
  read: boolean;
  createdAt: string;
};

export async function fetchNotifications(page = 0, size = 20): Promise<ApiPageResponse<Notification>> {
  const { data } = await api.get<ApiPageResponse<Notification>>("/notifications", { params: { page, size } });
  return data;
}

export async function fetchUnreadCount(): Promise<number> {
  const { data } = await api.get<{ count: number }>("/notifications/unread-count");
  return data.count;
}

export async function markNotificationRead(id: number): Promise<Notification> {
  const { data } = await api.patch<Notification>(`/notifications/${id}/read`);
  return data;
}
