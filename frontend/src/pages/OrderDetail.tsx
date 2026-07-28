import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import Container from "../components/ui/Container";
import PageHero from "../components/ui/PageHero";
import Reveal from "../components/ui/Reveal";
import { Button } from "../components/ui/Button";
import { cancelMyOrder, fetchMyOrder, type ApiOrder } from "../lib/orders";
import { extractErrorMessage } from "../lib/cart";
import { ORDER_STATUS_META } from "../lib/orderStatus";

export default function OrderDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<ApiOrder | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    fetchMyOrder(Number(id))
      .then((data) => {
        if (!cancelled) setOrder(data);
      })
      .catch((err) => {
        if (!cancelled) {
          if (axios.isAxiosError(err) && err.response?.status === 404) {
            navigate("/orders", { replace: true });
            return;
          }
          setError(extractErrorMessage(err, "Couldn't load this order."));
        }
      });
    return () => {
      cancelled = true;
    };
  }, [id, navigate]);

  async function handleCancel() {
    if (!order) return;
    setCancelling(true);
    setError(null);
    try {
      setOrder(await cancelMyOrder(order.id));
    } catch (err) {
      setError(extractErrorMessage(err, "Couldn't cancel this order."));
    } finally {
      setCancelling(false);
    }
  }

  if (error && !order) {
    return (
      <Container className="py-32 text-center">
        <p className="font-sans text-sm text-chili-500">{error}</p>
        <Link to="/orders" className="mt-4 inline-block font-sans text-xs font-bold uppercase tracking-wider text-gold-400 hover:underline">
          Back to Orders
        </Link>
      </Container>
    );
  }

  if (!order) {
    return (
      <p className="py-32 text-center font-sans text-sm uppercase tracking-widest text-paper-200/50">
        Loading order…
      </p>
    );
  }

  const meta = ORDER_STATUS_META[order.status];

  return (
    <>
      <PageHero
        kicker={`Order #${order.id}`}
        title={
          <>
            Order <span className="text-gold-400">Details</span>
          </>
        }
      />

      <Container className="pb-24 sm:pb-32">
        <div className="mx-auto max-w-3xl space-y-6">
          {error && (
            <p className="rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 text-center font-sans text-sm text-chili-500">
              {error}
            </p>
          )}

          <Reveal>
            <div className="card-elevate flex flex-wrap items-center justify-between gap-4 rounded-3xl border border-ink-600 bg-ink-900 p-8">
              <div>
                <span className={`rounded-full px-3 py-1 font-sans text-xs font-bold uppercase tracking-wider ${meta.className}`}>
                  {meta.label}
                </span>
                <p className="mt-2 font-sans text-xs text-paper-200/50">
                  Placed {new Date(order.createdAt).toLocaleString()}
                </p>
              </div>
              {order.status === "PENDING" && (
                <Button variant="outline" onClick={handleCancel} disabled={cancelling}>
                  {cancelling ? "Cancelling…" : "Cancel Order"}
                </Button>
              )}
            </div>
          </Reveal>

          <Reveal delay={0.05}>
            <div className="card-elevate rounded-3xl border border-ink-600 bg-ink-900 p-8">
              <h3 className="font-display text-xl uppercase tracking-wide text-gold-400">Items</h3>
              <ul className="mt-5 divide-y divide-ink-700">
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
          </Reveal>

          <Reveal delay={0.1}>
            <div className="card-elevate rounded-3xl border border-ink-600 bg-ink-900 p-8">
              <h3 className="font-display text-xl uppercase tracking-wide text-gold-400">Delivery Details</h3>
              <dl className="mt-5 space-y-2 font-sans text-sm text-paper-100/80">
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
          </Reveal>
        </div>
      </Container>
    </>
  );
}

