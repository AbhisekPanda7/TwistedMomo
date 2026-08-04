import { useEffect, useRef, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import Container from "../components/ui/Container";
import PageHero from "../components/ui/PageHero";
import Reveal from "../components/ui/Reveal";
import ErrorNote from "../components/ui/ErrorNote";
import { Button } from "../components/ui/Button";
import AddressPicker from "../components/checkout/AddressPicker";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { placeOrder } from "../lib/orders";
import { fetchSavedAddresses, type SavedAddress } from "../lib/addresses";
import { toApiError, type ApiError } from "../lib/apiError";

const inputClass =
  "w-full rounded-xl border border-ink-600 bg-ink-900 px-4 py-3 font-sans text-sm text-paper-50 placeholder:text-paper-200/30 outline-none transition-all duration-200 focus:border-gold-400 focus:ring-4 focus:ring-gold-400/10";

export default function Checkout() {
  const { user } = useAuth();
  const { cart, loading: cartLoading, refresh } = useCart();
  const navigate = useNavigate();

  const [recipientName, setRecipientName] = useState(user?.name ?? "");
  const [phone, setPhone] = useState(user?.phone ?? "");
  const [addressLine1, setAddressLine1] = useState("");
  const [addressLine2, setAddressLine2] = useState("");
  const [city, setCity] = useState("");
  const [postalCode, setPostalCode] = useState("");
  const [notes, setNotes] = useState("");
  const [error, setError] = useState<ApiError | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [savedAddresses, setSavedAddresses] = useState<SavedAddress[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);

  const hasUnavailable = cart?.items.some((item) => !item.available) ?? false;
  // Placing an order clears the cart server-side, which would otherwise re-trigger the
  // empty-cart guard below and yank the user back to /cart right as we navigate them
  // to their new order — this ref tells the guard to stand down once that's underway.
  const orderPlacedRef = useRef(false);

  useEffect(() => {
    if (orderPlacedRef.current) return;
    if (!cartLoading && (!cart || cart.items.length === 0)) {
      navigate("/cart", { replace: true });
    }
  }, [cartLoading, cart, navigate]);

  useEffect(() => {
    fetchSavedAddresses()
      .then((list) => {
        setSavedAddresses(list);
        // Most recently used first, so the default is the likeliest choice.
        if (list.length > 0) setSelectedAddressId(list[0].id);
      })
      .catch(() => setSavedAddresses([])); // A failure here must not block checkout.
  }, []);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const order = await placeOrder(
        selectedAddressId !== null
          ? { addressId: selectedAddressId, notes: notes || undefined }
          : {
              recipientName,
              phone,
              addressLine1,
              addressLine2: addressLine2 || undefined,
              city,
              postalCode,
              notes: notes || undefined,
            },
      );
      orderPlacedRef.current = true;
      await refresh();
      navigate(`/orders/${order.id}`, { replace: true });
    } catch (err) {
      setError(toApiError(err, "Couldn't place your order. Please try again."));
    } finally {
      setSubmitting(false);
    }
  }

  if (!cart || cart.items.length === 0) {
    return null;
  }

  return (
    <>
      <PageHero
        kicker="Almost There"
        title={
          <>
            Check<span className="text-gold-400">out</span>
          </>
        }
      />

      <Container className="pb-24 sm:pb-32">
        <div className="grid gap-10 lg:grid-cols-[1.3fr_1fr]">
          <Reveal>
            <form
              onSubmit={handleSubmit}
              className="card-elevate space-y-5 rounded-3xl border border-ink-600 bg-ink-900 p-8 sm:p-10"
            >
              <h3 className="font-display text-xl uppercase tracking-wide text-gold-400">Delivery Details</h3>

              {hasUnavailable && (
                <p className="rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">
                  One or more items in your cart are no longer available. Go back to your cart and remove them before checking out.
                </p>
              )}

              <ErrorNote error={error} />

              <AddressPicker
                addresses={savedAddresses}
                selectedId={selectedAddressId}
                onSelect={setSelectedAddressId}
              />

              {selectedAddressId === null && (
                <>
                  <div className="grid gap-5 sm:grid-cols-2">
                    <div>
                      <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                        Recipient Name
                      </label>
                      <input
                        required
                        value={recipientName}
                        onChange={(e) => setRecipientName(e.target.value)}
                        className={inputClass}
                        placeholder="Your name"
                      />
                    </div>
                    <div>
                      <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                        Phone
                      </label>
                      <input
                        required
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        className={inputClass}
                        placeholder="+91 00000 00000"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                      Address Line 1
                    </label>
                    <input
                      required
                      value={addressLine1}
                      onChange={(e) => setAddressLine1(e.target.value)}
                      className={inputClass}
                      placeholder="House no., street"
                    />
                  </div>

                  <div>
                    <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                      Address Line 2 <span className="normal-case text-paper-200/40">(optional)</span>
                    </label>
                    <input
                      value={addressLine2}
                      onChange={(e) => setAddressLine2(e.target.value)}
                      className={inputClass}
                      placeholder="Landmark, apartment, etc."
                    />
                  </div>

                  <div className="grid gap-5 sm:grid-cols-2">
                    <div>
                      <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                        City
                      </label>
                      <input
                        required
                        value={city}
                        onChange={(e) => setCity(e.target.value)}
                        className={inputClass}
                        placeholder="City"
                      />
                    </div>
                    <div>
                      <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                        Postal Code
                      </label>
                      <input
                        required
                        value={postalCode}
                        onChange={(e) => setPostalCode(e.target.value)}
                        className={inputClass}
                        placeholder="500001"
                      />
                    </div>
                  </div>
                </>
              )}

              <div>
                <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                  Notes <span className="normal-case text-paper-200/40">(optional)</span>
                </label>
                <textarea
                  rows={3}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className={inputClass}
                  placeholder="Delivery instructions, spice preference, etc."
                />
              </div>

              <Button type="submit" className="w-full" disabled={submitting || hasUnavailable}>
                {submitting ? "Placing Order…" : `Place Order — ₹${cart.subtotal}`}
              </Button>
            </form>
          </Reveal>

          <Reveal delay={0.1}>
            <div className="card-elevate sticky top-28 rounded-3xl border border-ink-600 bg-ink-900 p-8">
              <h3 className="font-display text-xl uppercase tracking-wide text-gold-400">Order Summary</h3>
              <ul className="mt-5 space-y-3 border-b border-ink-600 pb-5">
                {cart.items.map((item) => (
                  <li key={item.id} className="flex justify-between gap-3 font-sans text-sm text-paper-100/80">
                    <span className="min-w-0 truncate">
                      {item.menuItemName} <span className="text-paper-200/40">×{item.quantity}</span>
                    </span>
                    <span className="shrink-0 text-paper-50">₹{item.lineTotal}</span>
                  </li>
                ))}
              </ul>
              <div className="mt-4 flex justify-between font-sans text-sm font-bold text-paper-50">
                <span>Subtotal</span>
                <span>₹{cart.subtotal}</span>
              </div>
            </div>
          </Reveal>
        </div>
      </Container>
    </>
  );
}
