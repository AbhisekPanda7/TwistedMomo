import { api } from "./api";
import type { ApiCategory, ApiMenuItem, ApiPageResponse } from "./catalog";
import type { ApiOrder, ApiOrderSummary, OrderStatus } from "./orders";

export type CategoryPayload = {
  name: string;
  slug: string;
  description?: string;
  displayOrder?: number;
  active?: boolean;
};

export async function fetchAdminCategories(): Promise<ApiCategory[]> {
  const { data } = await api.get<ApiCategory[]>("/admin/categories");
  return data;
}

export async function createCategory(payload: CategoryPayload): Promise<ApiCategory> {
  const { data } = await api.post<ApiCategory>("/admin/categories", payload);
  return data;
}

export async function updateCategory(id: number, payload: CategoryPayload): Promise<ApiCategory> {
  const { data } = await api.put<ApiCategory>(`/admin/categories/${id}`, payload);
  return data;
}

export async function deleteCategory(id: number): Promise<void> {
  await api.delete(`/admin/categories/${id}`);
}

export type MenuItemPayload = {
  categoryId: number;
  name: string;
  slug: string;
  description?: string;
  price: number;
  veg: boolean;
  spicyLevel?: number;
  tag?: string;
  available?: boolean;
  displayOrder?: number;
};

export async function fetchAdminMenuItems(categoryId?: number): Promise<ApiMenuItem[]> {
  const { data } = await api.get<ApiPageResponse<ApiMenuItem>>("/admin/menu", {
    params: { size: 100, ...(categoryId ? { categoryId } : {}) },
  });
  return data.content;
}

export async function createMenuItem(payload: MenuItemPayload): Promise<ApiMenuItem> {
  const { data } = await api.post<ApiMenuItem>("/admin/menu", payload);
  return data;
}

export async function updateMenuItem(id: number, payload: MenuItemPayload): Promise<ApiMenuItem> {
  const { data } = await api.put<ApiMenuItem>(`/admin/menu/${id}`, payload);
  return data;
}

export async function deleteMenuItem(id: number): Promise<void> {
  await api.delete(`/admin/menu/${id}`);
}

// Availability toggling moved to /ops — RESTAURANT_EMP needs it during service, unlike menu CRUD.
export async function setMenuItemAvailability(id: number, available: boolean): Promise<ApiMenuItem> {
  const { data } = await api.patch<ApiMenuItem>(`/ops/menu/${id}/availability`, { available });
  return data;
}

export async function uploadMenuItemImage(id: number, file: File): Promise<ApiMenuItem> {
  const formData = new FormData();
  formData.append("file", file);
  const { data } = await api.post<ApiMenuItem>(`/admin/menu/${id}/image`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}

// Orders live under /ops — ADMIN and RESTAURANT_EMP both manage the queue from here.
export async function fetchAdminOrders(status?: OrderStatus | ""): Promise<ApiOrderSummary[]> {
  const { data } = await api.get<ApiPageResponse<ApiOrderSummary>>("/ops/orders", {
    params: { size: 100, ...(status ? { status } : {}) },
  });
  return data.content;
}

export async function fetchAdminOrder(id: number): Promise<ApiOrder> {
  const { data } = await api.get<ApiOrder>(`/ops/orders/${id}`);
  return data;
}

export async function updateOrderStatus(id: number, status: OrderStatus): Promise<ApiOrder> {
  const { data } = await api.patch<ApiOrder>(`/ops/orders/${id}/status`, { status });
  return data;
}
