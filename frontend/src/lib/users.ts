import { api } from "./api";
import type { ApiPageResponse } from "./catalog";

export type AdminUser = {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  roles: string[];
  enabled: boolean;
  emailVerified: boolean;
  createdAt: string;
};

export async function fetchAdminUsers(page = 0, size = 20): Promise<ApiPageResponse<AdminUser>> {
  const { data } = await api.get<ApiPageResponse<AdminUser>>("/admin/users", { params: { page, size } });
  return data;
}

export async function grantRole(userId: number, role: string): Promise<AdminUser> {
  const { data } = await api.post<AdminUser>(`/admin/users/${userId}/roles`, { role });
  return data;
}

export async function revokeRole(userId: number, role: string): Promise<AdminUser> {
  const { data } = await api.delete<AdminUser>(`/admin/users/${userId}/roles/${role}`);
  return data;
}
