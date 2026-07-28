import type { OrderStatus } from "./orders";

export const ORDER_STATUS_META: Record<OrderStatus, { label: string; className: string }> = {
  PENDING: { label: "Pending", className: "bg-paper-200/15 text-paper-100" },
  CONFIRMED: { label: "Confirmed", className: "bg-gold-400/20 text-gold-400" },
  PREPARING: { label: "Preparing", className: "bg-gold-400/20 text-gold-400" },
  READY: { label: "Ready", className: "bg-green-500/20 text-green-500" },
  OUT_FOR_DELIVERY: { label: "Out for Delivery", className: "bg-green-500/20 text-green-500" },
  DELIVERED: { label: "Delivered", className: "bg-green-500/20 text-green-500" },
  CANCELLED: { label: "Cancelled", className: "bg-chili-500/20 text-chili-500" },
};

/** Mirrors the backend's allow-list (OrderServiceImpl.ALLOWED_TRANSITIONS) — kept in sync by hand since it's a small, stable state machine. Purely a UX narrowing of the dropdown; the backend re-validates regardless. */
export const NEXT_STATUSES: Record<OrderStatus, OrderStatus[]> = {
  PENDING: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["PREPARING", "CANCELLED"],
  PREPARING: ["READY", "CANCELLED"],
  READY: ["OUT_FOR_DELIVERY", "CANCELLED"],
  OUT_FOR_DELIVERY: ["DELIVERED"],
  DELIVERED: [],
  CANCELLED: [],
};
