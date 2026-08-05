import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import AdminLayout from "../../components/admin/AdminLayout";
import Modal from "../../components/admin/Modal";
import { Button } from "../../components/ui/Button";
import { fetchAdminOrder, updateOrderStatus } from "../../lib/admin";
import { extractErrorMessage } from "../../lib/cart";
import { NEXT_STATUSES, ORDER_STATUS_META } from "../../lib/orderStatus";
import type { ApiOrder, OrderStatus } from "../../lib/orders";

const REASON_MAX_LENGTH = 255;

export default function AdminOrderDetail() {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<ApiOrder | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [nextStatus, setNextStatus] = useState<OrderStatus | "">("");
  const [updating, setUpdating] = useState(false);
  const [cancelReasonOpen, setCancelReasonOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState("");

  function load() {
    if (!id) return;
    fetchAdminOrder(Number(id))
      .then((data) => {
        setOrder(data);
        setNextStatus("");
      })
      .catch((err) => setError(extractErrorMessage(err, "Couldn't load this order.")));
  }

  useEffect(load, [id]);

  async function submitStatusUpdate(status: OrderStatus, reason?: string) {
    if (!order) return;
    setUpdating(true);
    setError(null);
    try {
      const updated = await updateOrderStatus(order.id, status, reason);
      setOrder(updated);
      setNextStatus("");
    } catch (err) {
      setError(extractErrorMessage(err, "Couldn't update the order status."));
    } finally {
      setUpdating(false);
    }
  }

  function handleUpdateStatus() {
    if (!nextStatus) return;
    if (nextStatus === "CANCELLED") {
      setCancelReason("");
      setCancelReasonOpen(true);
      return;
    }
    void submitStatusUpdate(nextStatus);
  }

  function handleConfirmCancel() {
    setCancelReasonOpen(false);
    void submitStatusUpdate("CANCELLED", cancelReason.trim() || undefined);
  }

  if (error && !order) {
    return (
      <AdminLayout>
        <p className="rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">{error}</p>
        <Link to="/admin/orders" className="mt-4 inline-block font-sans text-xs font-bold uppercase tracking-wider text-gold-400 hover:underline">
          Back to Orders
        </Link>
      </AdminLayout>
    );
  }

  if (!order) {
    return (
      <AdminLayout>
        <p className="font-sans text-sm uppercase tracking-widest text-paper-200/50">Loading…</p>
      </AdminLayout>
    );
  }

  const meta = ORDER_STATUS_META[order.status];
  const options = NEXT_STATUSES[order.status];

  return (
    <AdminLayout>
      <Link to="/admin/orders" className="mb-6 inline-block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60 hover:text-gold-400">
        ← Back to Orders
      </Link>

      {error && (
        <p className="mb-6 rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">{error}</p>
      )}

      <div className="grid gap-6 lg:grid-cols-[1.4fr_1fr]">
        <div className="space-y-6">
          <div className="rounded-3xl border border-ink-600 bg-ink-900 p-7">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div>
                <h2 className="font-display text-2xl text-paper-50">Order #{order.id}</h2>
                <p className="mt-1 font-sans text-xs text-paper-200/50">Placed {new Date(order.createdAt).toLocaleString()}</p>
              </div>
              <span className={`rounded-full px-3 py-1 font-sans text-xs font-bold uppercase tracking-wider ${meta.className}`}>{meta.label}</span>
            </div>

            <ul className="mt-6 divide-y divide-ink-700">
              {order.items.map((item) => (
                <li key={item.id} className="flex items-center justify-between gap-4 py-3">
                  <div className="min-w-0">
                    <p className="truncate font-sans text-sm font-medium text-paper-50">{item.menuItemName}</p>
                    <p className="font-sans text-xs text-paper-200/50">
                      ₹{item.unitPrice} × {item.quantity}
                    </p>
                  </div>
                  <span className="shrink-0 font-display text-base text-gold-400">₹{item.lineTotal}</span>
                </li>
              ))}
            </ul>
            <div className="mt-4 flex justify-between border-t border-ink-600 pt-4 font-sans text-sm font-bold text-paper-50">
              <span>Subtotal</span>
              <span>₹{order.subtotal}</span>
            </div>
          </div>

          <div className="rounded-3xl border border-ink-600 bg-ink-900 p-7">
            <h3 className="font-display text-lg uppercase tracking-wide text-gold-400">Delivery Details</h3>
            <dl className="mt-4 space-y-2 font-sans text-sm text-paper-100/80">
              <div className="flex justify-between gap-4">
                <dt className="text-paper-200/50">Recipient</dt>
                <dd className="text-right">{order.recipientName}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-paper-200/50">Phone</dt>
                <dd className="text-right">{order.phone}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-paper-200/50">Address</dt>
                <dd className="text-right">
                  {order.addressLine1}
                  {order.addressLine2 ? `, ${order.addressLine2}` : ""}, {order.city} {order.postalCode}
                </dd>
              </div>
              {order.notes && (
                <div className="flex justify-between gap-4">
                  <dt className="text-paper-200/50">Notes</dt>
                  <dd className="text-right">{order.notes}</dd>
                </div>
              )}
            </dl>
          </div>
        </div>

        <div className="space-y-6">
          <div className="rounded-3xl border border-ink-600 bg-ink-900 p-7">
            <h3 className="font-display text-lg uppercase tracking-wide text-gold-400">Customer</h3>
            <p className="mt-3 font-sans text-sm text-paper-100/80">{order.customerName}</p>
            <p className="font-sans text-sm text-paper-200/50">{order.customerEmail}</p>
          </div>

          <div className="rounded-3xl border border-ink-600 bg-ink-900 p-7">
            <h3 className="font-display text-lg uppercase tracking-wide text-gold-400">Update Status</h3>
            {options.length === 0 ? (
              <p className="mt-3 font-sans text-sm text-paper-200/50">This order is in a final state.</p>
            ) : (
              <div className="mt-4 space-y-3">
                <select
                  value={nextStatus}
                  onChange={(e) => setNextStatus(e.target.value as OrderStatus)}
                  className="w-full rounded-xl border border-ink-600 bg-ink-950 px-4 py-2.5 font-sans text-sm text-paper-50 outline-none focus:border-gold-400 focus:ring-4 focus:ring-gold-400/10"
                >
                  <option value="">Select next status…</option>
                  {options.map((s) => (
                    <option key={s} value={s}>
                      {ORDER_STATUS_META[s].label}
                    </option>
                  ))}
                </select>
                <Button className="w-full" disabled={!nextStatus || updating} onClick={handleUpdateStatus}>
                  {updating ? "Updating…" : "Update Status"}
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>

      <Modal open={cancelReasonOpen} onClose={() => setCancelReasonOpen(false)} title="Cancel Order">
        <p className="font-sans text-sm text-paper-200/60">
          Let the customer know why — optional, but it replaces the bare "Order cancelled" they'd see otherwise.
        </p>
        <textarea
          value={cancelReason}
          onChange={(e) => setCancelReason(e.target.value.slice(0, REASON_MAX_LENGTH))}
          maxLength={REASON_MAX_LENGTH}
          rows={3}
          placeholder="e.g. Kitchen ran out of an ingredient"
          className="mt-4 w-full rounded-xl border border-ink-600 bg-ink-950 px-4 py-2.5 font-sans text-sm text-paper-50 outline-none focus:border-gold-400 focus:ring-4 focus:ring-gold-400/10"
        />
        <p className="mt-1 text-right font-sans text-xs text-paper-200/40">
          {cancelReason.length}/{REASON_MAX_LENGTH}
        </p>
        <div className="mt-4 flex justify-end gap-3">
          <Button variant="outline" onClick={() => setCancelReasonOpen(false)}>
            Back
          </Button>
          <Button onClick={handleConfirmCancel} disabled={updating}>
            {updating ? "Cancelling…" : "Confirm Cancellation"}
          </Button>
        </div>
      </Modal>
    </AdminLayout>
  );
}
