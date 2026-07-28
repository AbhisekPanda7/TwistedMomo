import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Container from "../components/ui/Container";
import PageHero from "../components/ui/PageHero";
import Reveal from "../components/ui/Reveal";
import { ButtonLink } from "../components/ui/Button";
import { DumplingIcon } from "../components/ui/Icons";
import { fetchMyOrders, type ApiOrderSummary } from "../lib/orders";
import { extractErrorMessage } from "../lib/cart";
import { ORDER_STATUS_META } from "../lib/orderStatus";

export default function Orders() {
  const [orders, setOrders] = useState<ApiOrderSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchMyOrders()
      .then((data) => {
        if (!cancelled) setOrders(data);
      })
      .catch((err) => {
        if (!cancelled) setError(extractErrorMessage(err, "Couldn't load your orders."));
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <>
      <PageHero
        kicker="Order History"
        title={
          <>
            Your <span className="text-gold-400">Orders</span>
          </>
        }
      />

      <Container className="pb-24 sm:pb-32">
        {!orders && !error && (
          <p className="py-16 text-center font-sans text-sm uppercase tracking-widest text-paper-200/50">
            Loading your orders…
          </p>
        )}

        {error && (
          <p className="mx-auto max-w-xl rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 text-center font-sans text-sm text-chili-500">
            {error}
          </p>
        )}

        {orders && orders.length === 0 && (
          <Reveal>
            <div className="mx-auto flex max-w-md flex-col items-center py-16 text-center">
              <DumplingIcon className="h-14 w-14 text-gold-400/40" />
              <h2 className="mt-6 font-display text-2xl uppercase text-paper-50">No orders yet</h2>
              <p className="mt-2 font-sans text-sm text-paper-200/60">Your first twist is one click away.</p>
              <div className="mt-8">
                <ButtonLink to="/menu">Browse The Menu</ButtonLink>
              </div>
            </div>
          </Reveal>
        )}

        {orders && orders.length > 0 && (
          <div className="mx-auto max-w-3xl space-y-3">
            {orders.map((order) => {
              const meta = ORDER_STATUS_META[order.status];
              return (
                <Link
                  key={order.id}
                  to={`/orders/${order.id}`}
                  data-cursor-hover
                  className="card-elevate flex items-center justify-between gap-4 rounded-2xl border border-ink-600 bg-ink-900 p-5 transition-colors hover:border-gold-400/60"
                >
                  <div className="min-w-0">
                    <div className="flex items-center gap-3">
                      <span className="font-sans text-sm font-bold text-paper-50">Order #{order.id}</span>
                      <span className={`rounded-full px-2.5 py-0.5 font-sans text-[10px] font-bold uppercase tracking-wider ${meta.className}`}>
                        {meta.label}
                      </span>
                    </div>
                    <p className="mt-1 font-sans text-xs text-paper-200/50">
                      {new Date(order.createdAt).toLocaleString()} · {order.totalItems} item{order.totalItems === 1 ? "" : "s"}
                    </p>
                  </div>
                  <span className="shrink-0 font-display text-lg text-gold-400">₹{order.subtotal}</span>
                </Link>
              );
            })}
          </div>
        )}
      </Container>
    </>
  );
}
