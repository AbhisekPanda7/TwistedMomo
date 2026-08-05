import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import AdminLayout from "../../components/admin/AdminLayout";
import { fetchAdminOrders } from "../../lib/admin";
import { extractErrorMessage } from "../../lib/cart";
import { ORDER_STATUS_META } from "../../lib/orderStatus";
import type { ApiOrderSummary, OrderStatus } from "../../lib/orders";

const inputClass =
  "w-auto rounded-xl border border-ink-600 bg-ink-950 px-4 py-2.5 font-sans text-sm text-paper-50 outline-none transition-all duration-200 focus:border-gold-400 focus:ring-4 focus:ring-gold-400/10";

const STATUS_OPTIONS: OrderStatus[] = ["PENDING", "CONFIRMED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"];

export default function AdminOrders() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const statusFilter = (searchParams.get("status") as OrderStatus | null) ?? "";
  const [orders, setOrders] = useState<ApiOrderSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setOrders(null);
    fetchAdminOrders(statusFilter || undefined)
      .then(setOrders)
      .catch((err) => setError(extractErrorMessage(err, "Couldn't load orders.")));
  }, [statusFilter]);

  // No client can hold the ops SSE stream open (EventSource can't send the bearer JWT it
  // requires), so this polls instead — same refetch-heals-any-gap contract, just on a timer.
  useEffect(() => {
    const intervalId = setInterval(() => {
      fetchAdminOrders(statusFilter || undefined)
        .then(setOrders)
        .catch(() => {
          // A background refresh failing shouldn't replace the list the operator is looking at.
        });
    }, 15000);
    return () => clearInterval(intervalId);
  }, [statusFilter]);

  return (
    <AdminLayout>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h2 className="font-display text-xl uppercase tracking-wide text-paper-50">Orders</h2>
        <select
          value={statusFilter}
          onChange={(e) => setSearchParams(e.target.value ? { status: e.target.value } : {})}
          className={inputClass}
        >
          <option value="">All statuses</option>
          {STATUS_OPTIONS.map((s) => (
            <option key={s} value={s}>
              {ORDER_STATUS_META[s].label}
            </option>
          ))}
        </select>
      </div>

      {error && (
        <p className="mb-4 rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">
          {error}
        </p>
      )}

      {!orders && !error && <p className="font-sans text-sm uppercase tracking-widest text-paper-200/50">Loading…</p>}

      {orders && (
        <div className="overflow-x-auto rounded-2xl border border-ink-600">
          <table className="w-full min-w-[760px] border-collapse font-sans text-sm">
            <thead>
              <tr className="border-b border-ink-600 bg-ink-900 text-left text-xs font-bold uppercase tracking-wider text-paper-200/50">
                <th className="px-4 py-3">Order</th>
                <th className="px-4 py-3">Customer</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Items</th>
                <th className="px-4 py-3">Total</th>
                <th className="px-4 py-3">Placed</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => {
                const meta = ORDER_STATUS_META[order.status];
                return (
                  <tr
                    key={order.id}
                    onClick={() => navigate(`/admin/orders/${order.id}`)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter" || e.key === " ") {
                        e.preventDefault();
                        navigate(`/admin/orders/${order.id}`);
                      }
                    }}
                    tabIndex={0}
                    data-cursor-hover
                    className="cursor-pointer border-b border-ink-700 bg-ink-950 transition-colors last:border-0 hover:bg-ink-900 focus:bg-ink-900 focus:outline-none"
                  >
                    <td className="px-4 py-3 font-sans text-sm font-bold text-gold-400">#{order.id}</td>
                    <td className="px-4 py-3 text-paper-100/80">
                      <p>{order.customerName}</p>
                      <p className="text-xs text-paper-200/40">{order.customerEmail}</p>
                    </td>
                    <td className="px-4 py-3">
                      <span className={`rounded-full px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider ${meta.className}`}>{meta.label}</span>
                    </td>
                    <td className="px-4 py-3 text-paper-200/60">{order.totalItems}</td>
                    <td className="px-4 py-3 text-gold-400">₹{order.subtotal}</td>
                    <td className="px-4 py-3 text-paper-200/50">{new Date(order.createdAt).toLocaleString()}</td>
                  </tr>
                );
              })}
              {orders.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-paper-200/50">
                    No orders found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </AdminLayout>
  );
}
