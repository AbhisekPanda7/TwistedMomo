import { api } from "./api";
import type { ApiPageResponse } from "./catalog";

export type ApiOrderItem = {
  id: number;
  menuItemId: number | null;
  menuItemName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
};

export type OrderStatus =
  | "PENDING"
  | "CONFIRMED"
  | "PREPARING"
  | "READY"
  | "OUT_FOR_DELIVERY"
  | "DELIVERED"
  | "CANCELLED";

export type ApiOrder = {
  id: number;
  status: OrderStatus;
  items: ApiOrderItem[];
  totalItems: number;
  subtotal: number;
  recipientName: string;
  phone: string;
  addressLine1: string;
  addressLine2: string | null;
  city: string;
  postalCode: string;
  notes: string | null;
  customerName: string;
  customerEmail: string;
  createdAt: string;
  updatedAt: string;
  cancelledBy: string | null;
  cancellationReason: string | null;
};

export type ApiOrderSummary = {
  id: number;
  status: OrderStatus;
  totalItems: number;
  subtotal: number;
  customerName: string;
  customerEmail: string;
  createdAt: string;
};

export type PlaceOrderPayload =
  | {
      addressId: number;
      notes?: string;
    }
  | {
      recipientName: string;
      phone: string;
      addressLine1: string;
      addressLine2?: string;
      city: string;
      postalCode: string;
      notes?: string;
    };

export async function placeOrder(payload: PlaceOrderPayload): Promise<ApiOrder> {
  const { data } = await api.post<ApiOrder>("/orders", payload);
  return data;
}

export async function fetchMyOrders(): Promise<ApiOrderSummary[]> {
  const { data } = await api.get<ApiPageResponse<ApiOrderSummary>>("/orders", { params: { size: 50 } });
  return data.content;
}

export async function fetchMyOrder(id: number): Promise<ApiOrder> {
  const { data } = await api.get<ApiOrder>(`/orders/${id}`);
  return data;
}

export async function cancelMyOrder(id: number): Promise<ApiOrder> {
  const { data } = await api.patch<ApiOrder>(`/orders/${id}/cancel`);
  return data;
}
