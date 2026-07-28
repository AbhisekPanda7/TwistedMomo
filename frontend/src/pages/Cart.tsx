import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import Container from "../components/ui/Container";
import PageHero from "../components/ui/PageHero";
import Reveal from "../components/ui/Reveal";
import { Button, ButtonLink } from "../components/ui/Button";
import { DumplingIcon } from "../components/ui/Icons";
import { useCart } from "../context/CartContext";
import type { ApiCartItem } from "../lib/cart";

const MAX_QUANTITY = 20;

function QuantityStepper({
  item,
  onChange,
  disabled,
}: {
  item: ApiCartItem;
  onChange: (quantity: number) => void;
  disabled: boolean;
}) {
  return (
    <div className="flex items-center gap-3 rounded-full border border-ink-600 px-1 py-1">
      <button
        type="button"
        disabled={disabled}
        onClick={() => onChange(item.quantity - 1)}
        data-cursor-hover
        className="flex h-7 w-7 items-center justify-center rounded-full font-sans text-sm font-bold text-paper-100 transition-colors hover:bg-ink-700 disabled:opacity-40"
        aria-label="Decrease quantity"
      >
        −
      </button>
      <span className="w-5 text-center font-sans text-sm font-bold text-paper-50">{item.quantity}</span>
      <button
        type="button"
        disabled={disabled || item.quantity >= MAX_QUANTITY}
        onClick={() => onChange(item.quantity + 1)}
        data-cursor-hover
        className="flex h-7 w-7 items-center justify-center rounded-full font-sans text-sm font-bold text-paper-100 transition-colors hover:bg-ink-700 disabled:opacity-40"
        aria-label="Increase quantity"
      >
        +
      </button>
    </div>
  );
}

export default function Cart() {
  const { cart, loading, error, updateItem, removeItem, clear } = useCart();
  const navigate = useNavigate();
  const [busyItemId, setBusyItemId] = useState<number | null>(null);
  const [clearing, setClearing] = useState(false);
  const hasUnavailable = cart?.items.some((item) => !item.available) ?? false;

  async function handleQuantityChange(item: ApiCartItem, quantity: number) {
    setBusyItemId(item.id);
    try {
      if (quantity <= 0) {
        await removeItem(item.id);
      } else {
        await updateItem(item.id, quantity);
      }
    } catch {
      // error surfaced via cart.error below
    } finally {
      setBusyItemId(null);
    }
  }

  async function handleRemove(item: ApiCartItem) {
    setBusyItemId(item.id);
    try {
      await removeItem(item.id);
    } catch {
      // error surfaced via cart.error below
    } finally {
      setBusyItemId(null);
    }
  }

  async function handleClear() {
    setClearing(true);
    try {
      await clear();
    } catch {
      // error surfaced via cart.error below
    } finally {
      setClearing(false);
    }
  }

  const isEmpty = !loading && (!cart || cart.items.length === 0);

  return (
    <>
      <PageHero
        kicker="Your Order"
        title={
          <>
            Your <span className="text-gold-400">Cart</span>
          </>
        }
      />

      <Container className="pb-24 sm:pb-32">
        {loading && (
          <p className="py-16 text-center font-sans text-sm uppercase tracking-widest text-paper-200/50">
            Loading your cart…
          </p>
        )}

        {error && (
          <p className="mx-auto mb-6 max-w-xl rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 text-center font-sans text-sm text-chili-500">
            {error}
          </p>
        )}

        {isEmpty && (
          <Reveal>
            <div className="mx-auto flex max-w-md flex-col items-center py-16 text-center">
              <DumplingIcon className="h-14 w-14 text-gold-400/40" />
              <h2 className="mt-6 font-display text-2xl uppercase text-paper-50">Your cart's empty</h2>
              <p className="mt-2 font-sans text-sm text-paper-200/60">
                Go fold some flavour into it — the menu's waiting.
              </p>
              <div className="mt-8">
                <ButtonLink to="/menu">Browse The Menu</ButtonLink>
              </div>
            </div>
          </Reveal>
        )}

        {cart && cart.items.length > 0 && (
          <div className="grid gap-10 lg:grid-cols-[1.4fr_1fr]">
            <div className="space-y-3">
              <AnimatePresence initial={false}>
                {cart.items.map((item) => (
                  <motion.div
                    key={item.id}
                    layout
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, x: -20 }}
                    transition={{ duration: 0.3 }}
                    className="card-elevate flex items-center justify-between gap-4 rounded-2xl border border-ink-600 bg-ink-900 p-5"
                  >
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="truncate font-sans text-[15px] font-medium text-paper-50">{item.menuItemName}</span>
                        {!item.available && (
                          <span className="rounded-full bg-chili-500/20 px-2 py-0.5 font-sans text-[9px] font-bold uppercase tracking-wider text-chili-500">
                            Unavailable
                          </span>
                        )}
                      </div>
                      <p className="mt-1 font-sans text-xs text-paper-200/50">₹{item.unitPrice} each</p>
                    </div>

                    <div className="flex shrink-0 items-center gap-4">
                      <QuantityStepper
                        item={item}
                        disabled={busyItemId === item.id}
                        onChange={(q) => handleQuantityChange(item, q)}
                      />
                      <span className="w-16 shrink-0 text-right font-display text-lg text-gold-400">
                        ₹{item.lineTotal}
                      </span>
                      <button
                        type="button"
                        onClick={() => handleRemove(item)}
                        disabled={busyItemId === item.id}
                        data-cursor-hover
                        className="font-sans text-xs font-bold uppercase tracking-wider text-paper-200/40 transition-colors hover:text-chili-500 disabled:opacity-40"
                        aria-label={`Remove ${item.menuItemName}`}
                      >
                        Remove
                      </button>
                    </div>
                  </motion.div>
                ))}
              </AnimatePresence>

              <button
                type="button"
                onClick={handleClear}
                disabled={clearing}
                data-cursor-hover
                className="mt-2 font-sans text-xs font-bold uppercase tracking-wider text-paper-200/50 transition-colors hover:text-chili-500 disabled:opacity-40"
              >
                {clearing ? "Clearing…" : "Clear Cart"}
              </button>
            </div>

            <Reveal delay={0.1}>
              <div className="card-elevate sticky top-28 rounded-3xl border border-ink-600 bg-ink-900 p-8">
                <h3 className="font-display text-xl uppercase tracking-wide text-gold-400">Order Summary</h3>
                <div className="mt-5 space-y-2 border-b border-ink-600 pb-5 font-sans text-sm text-paper-100/80">
                  <div className="flex justify-between">
                    <span>Items</span>
                    <span>{cart.totalItems}</span>
                  </div>
                  <div className="flex justify-between font-bold text-paper-50">
                    <span>Subtotal</span>
                    <span>₹{cart.subtotal}</span>
                  </div>
                </div>
                <Button className="mt-6 w-full" disabled={hasUnavailable} onClick={() => navigate("/checkout")}>
                  Proceed to Checkout
                </Button>
                {hasUnavailable && (
                  <p className="mt-3 text-center font-sans text-xs text-chili-500">
                    Remove unavailable items before checking out.
                  </p>
                )}
              </div>
            </Reveal>
          </div>
        )}
      </Container>
    </>
  );
}
