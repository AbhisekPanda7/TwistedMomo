import type { SavedAddress } from "../../lib/addresses";

/** Selecting an address hides the form; "a different address" brings it back. */
export default function AddressPicker({
  addresses,
  selectedId,
  onSelect,
}: {
  addresses: SavedAddress[];
  selectedId: number | null;
  onSelect: (id: number | null) => void;
}) {
  if (addresses.length === 0) return null;

  return (
    <div className="space-y-3">
      <p className="font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
        Deliver to
      </p>

      {addresses.map((address) => (
        <button
          key={address.id}
          type="button"
          onClick={() => onSelect(address.id)}
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
        onClick={() => onSelect(null)}
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
  );
}
