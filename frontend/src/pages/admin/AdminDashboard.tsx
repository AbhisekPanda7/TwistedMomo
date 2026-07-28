import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AdminLayout from "../../components/admin/AdminLayout";
import { fetchAdminCategories, fetchAdminMenuItems, fetchAdminOrders } from "../../lib/admin";
import { extractErrorMessage } from "../../lib/cart";
import { ORDER_STATUS_META } from "../../lib/orderStatus";
import type { ApiOrderSummary } from "../../lib/orders";

type Stats = {
  categoryCount: number;
  menuItemCount: number;
  unavailableCount: number;
  pendingOrders: ApiOrderSummary[];
  orderCount: number;
};

export default function AdminDashboard() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    Promise.all([fetchAdminCategories(), fetchAdminMenuItems(), fetchAdminOrders()])
      .then(([categories, menuItems, orders]) => {
        if (cancelled) return;
        setStats({
          categoryCount: categories.length,
          menuItemCount: menuItems.length,
          unavailableCount: menuItems.filter((i) => !i.available).length,
          orderCount: orders.length,
          pendingOrders: orders.filter((o) => o.status === "PENDING").slice(0, 5),
        });
      })
      .catch((err) => {
        if (!cancelled) setError(extractErrorMessage(err, "Couldn't load dashboard data."));
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <AdminLayout>
      {error && (
        <p className="mb-6 rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">
          {error}
        </p>
      )}

      {!stats && !error && (
        <p className="font-sans text-sm uppercase tracking-widest text-paper-200/50">Loading…</p>
      )}

      {stats && (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <StatCard label="Categories" value={stats.categoryCount} to="/admin/categories" />
            <StatCard label="Menu Items" value={stats.menuItemCount} to="/admin/menu" />
            <StatCard label="Unavailable Items" value={stats.unavailableCount} to="/admin/menu" accent={stats.unavailableCount > 0} />
            <StatCard label="Total Orders" value={stats.orderCount} to="/admin/orders" />
          </div>

          <div className="mt-8 rounded-3xl border border-ink-600 bg-ink-900 p-7">
            <div className="flex items-center justify-between">
              <h2 className="font-display text-lg uppercase tracking-wide text-gold-400">Pending Orders</h2>
              <Link to="/admin/orders?status=PENDING" className="font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60 hover:text-gold-400">
                View all
              </Link>
            </div>
            {stats.pendingOrders.length === 0 ? (
              <p className="mt-4 font-sans text-sm text-paper-200/50">Nothing pending right now.</p>
            ) : (
              <ul className="mt-4 divide-y divide-ink-700">
                {stats.pendingOrders.map((order) => (
                  <li key={order.id}>
                    <Link
                      to={`/admin/orders/${order.id}`}
                      className="flex items-center justify-between gap-4 py-3 font-sans text-sm text-paper-100/80 hover:text-gold-400"
                    >
                      <span>
                        #{order.id} · {order.customerName}
                      </span>
                      <span className="flex items-center gap-3">
                        <span className={`rounded-full px-2.5 py-0.5 font-sans text-[10px] font-bold uppercase tracking-wider ${ORDER_STATUS_META[order.status].className}`}>
                          {ORDER_STATUS_META[order.status].label}
                        </span>
                        <span className="text-gold-400">₹{order.subtotal}</span>
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      )}
    </AdminLayout>
  );
}

function StatCard({ label, value, to, accent = false }: { label: string; value: number; to: string; accent?: boolean }) {
  return (
    <Link
      to={to}
      className="block rounded-2xl border border-ink-600 bg-ink-900 p-6 transition-colors hover:border-gold-400/60"
    >
      <p className="font-sans text-xs font-bold uppercase tracking-wider text-paper-200/50">{label}</p>
      <p className={`mt-2 font-display text-3xl ${accent ? "text-chili-500" : "text-gold-400"}`}>{value}</p>
    </Link>
  );
}
