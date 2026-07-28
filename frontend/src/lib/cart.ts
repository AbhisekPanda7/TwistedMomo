import axios from "axios";
import { api } from "./api";

export type ApiCartItem = {
  id: number;
  menuItemId: number;
  menuItemName: string;
  menuItemImageUrl: string | null;
  unitPrice: number;
  available: boolean;
  quantity: number;
  lineTotal: number;
};

export type ApiCart = {
  id: number;
  items: ApiCartItem[];
  totalItems: number;
  subtotal: number;
};

export async function fetchCart(): Promise<ApiCart> {
  const { data } = await api.get<ApiCart>("/cart");
  return data;
}

export async function addCartItem(menuItemId: number, quantity: number): Promise<ApiCart> {
  const { data } = await api.post<ApiCart>("/cart/items", { menuItemId, quantity });
  return data;
}

export async function updateCartItem(cartItemId: number, quantity: number): Promise<ApiCart> {
  const { data } = await api.patch<ApiCart>(`/cart/items/${cartItemId}`, { quantity });
  return data;
}

export async function removeCartItem(cartItemId: number): Promise<ApiCart> {
  const { data } = await api.delete<ApiCart>(`/cart/items/${cartItemId}`);
  return data;
}

export async function clearCart(): Promise<ApiCart> {
  const { data } = await api.delete<ApiCart>("/cart");
  return data;
}

export function extractErrorMessage(err: unknown, fallback: string): string {
  if (axios.isAxiosError(err) && typeof err.response?.data?.message === "string") {
    return err.response.data.message;
  }
  return fallback;
}
