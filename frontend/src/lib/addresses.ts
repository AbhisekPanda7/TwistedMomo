import { api } from "./api";

export type SavedAddress = {
  id: number;
  recipientName: string;
  phone: string;
  addressLine1: string;
  addressLine2: string | null;
  city: string;
  postalCode: string;
  lastUsedAt: string;
};

export async function fetchSavedAddresses(): Promise<SavedAddress[]> {
  const { data } = await api.get<SavedAddress[]>("/addresses");
  return data;
}
