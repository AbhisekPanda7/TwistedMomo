import { useState } from "react";
import type { SavedAddress } from "../../lib/addresses";

/**
 * The selected address is always shown as a "Delivering to" summary, even while
 * the option list is open — a returning customer must never lose sight of where
 * the order is actually going. Expanding the list to change it is one click away.
 */
export default function AddressPicker({
  addresses,
  selectedId,
  onSelect,
}: {
  addresses: SavedAddress[];
  selectedId: number | null;
  onSelect: (id: number | null) => void;
}) {
  const [expanded, setExpanded] = useState(false);

  if (addresses.length === 0) return null;

  const selectedAddress = addresses.find((address) => address.id === selectedId) ?? null;

  return (
    <div className="space-y-3">
      <p className="font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">Deliver to</p>

      {selectedAddress && (
        <div className="rounded-2xl border border-gold-400 bg-gold-400/10 p-4">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <div className="font-sans text-[11px] font-bold uppercase tracking-wider text-gold-400">
                Delivering to
              </div>
              <div className="mt-1.5 font-sans text-sm font-semibold text-paper-50">
                {selectedAddress.recipientName}
              </div>
              <div className="mt-1 font-sans text-sm text-paper-100/80">
                {selectedAddress.addressLine1}
                {selectedAddress.addressLine2 ? `, ${selectedAddress.addressLine2}` : ""}, {selectedAddress.city}{" "}
                {selectedAddress.postalCode}
              </div>
            </div>
            <button
              type="button"
              onClick={() => setExpanded((v) => !v)}
              data-cursor-hover
              className="shrink-0 rounded-full border border-gold-400/40 px-3 py-1.5 font-sans text-xs font-bold uppercase tracking-wider text-gold-400 transition-colors hover:bg-gold-400/10"
            >
              {expanded ? "Close" : "Change"}
            </button>
          </div>
        </div>
      )}

      {(expanded || !selectedAddress) && (
        <div className="space-y-3">
          {addresses.map((address) => (
            <button
              key={address.id}
              type="button"
              onClick={() => {
                onSelect(address.id);
                setExpanded(false);
              }}
              data-cursor-hover
              className={`w-full rounded-2xl border p-4 text-left transition-colors ${
                selectedId === address.id
                  ? "border-gold-400 bg-gold-400/10"
                  : "border-ink-600 bg-ink-900 hover:border-gold-400/40"
              }`}
            >
              <div className="font-sans text-sm font-semibold text-paper-50">{address.recipientName}</div>
              <div className="mt-1 font-sans text-xs text-paper-200/60">
                {address.addressLine1}
                {address.addressLine2 ? `, ${address.addressLine2}` : ""}, {address.city} {address.postalCode}
              </div>
              <div className="mt-1 font-sans text-xs text-paper-200/40">{address.phone}</div>
            </button>
          ))}

          <button
            type="button"
            onClick={() => {
              onSelect(null);
              setExpanded(false);
            }}
            data-cursor-hover
            className={`w-full rounded-2xl border p-3 text-center font-sans text-sm transition-colors ${
              selectedId === null
                ? "border-gold-400 bg-gold-400/10 text-paper-50"
                : "border-ink-600 text-paper-200/60 hover:border-gold-400/40"
            }`}
          >
            Use a different address
          </button>
        </div>
      )}
    </div>
  );
}
