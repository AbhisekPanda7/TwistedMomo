import type { OrderStatus } from "../../lib/orders";

const TRACK = ["PENDING", "CONFIRMED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED"] as const;

const LABELS: Record<(typeof TRACK)[number], string> = {
  PENDING: "Placed",
  CONFIRMED: "Confirmed",
  PREPARING: "Preparing",
  READY: "Ready",
  OUT_FOR_DELIVERY: "On the way",
  DELIVERED: "Delivered",
};

type Props = {
  status: OrderStatus;
  cancellationReason?: string | null;
};

/** Reads order.status directly — rebuilding position from the notification feed would break the moment a row is missing. */
export default function OrderProgress({ status, cancellationReason }: Props) {
  if (status === "CANCELLED") {
    return (
      <div className="rounded-3xl border border-chili-500/40 bg-chili-500/10 p-6">
        <p className="font-sans text-sm font-semibold text-chili-500">Order cancelled</p>
        {cancellationReason && (
          <p className="mt-1 font-sans text-xs text-paper-200/70">{cancellationReason}</p>
        )}
      </div>
    );
  }

  const current = TRACK.indexOf(status as (typeof TRACK)[number]);

  return (
    <div className="rounded-3xl border border-ink-600 bg-ink-900 p-6">
      <div className="flex items-center gap-1">
        {TRACK.map((step, index) => {
          const done = index <= current;
          return (
            <div key={step} className="flex flex-1 flex-col items-center gap-1.5">
              <div
                className={`h-1.5 w-full rounded-full transition-colors ${
                  done ? "bg-gold-400" : "bg-ink-600"
                }`}
              />
              <span
                className={`text-center font-sans text-[10px] uppercase tracking-wide ${
                  index === current ? "text-gold-400" : "text-paper-200/40"
                }`}
              >
                {LABELS[step]}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
