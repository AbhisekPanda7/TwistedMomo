import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { useAuth } from "./AuthContext";
import {
  addCartItem,
  clearCart as clearCartRequest,
  extractErrorMessage,
  fetchCart,
  removeCartItem,
  updateCartItem,
  type ApiCart,
} from "../lib/cart";

type CartContextValue = {
  cart: ApiCart | null;
  loading: boolean;
  error: string | null;
  addItem: (menuItemId: number, quantity?: number) => Promise<void>;
  updateItem: (cartItemId: number, quantity: number) => Promise<void>;
  removeItem: (cartItemId: number) => Promise<void>;
  clear: () => Promise<void>;
  /** Re-syncs local state with the server — for when something else (e.g. placing an order) changed the cart server-side. */
  refresh: () => Promise<void>;
};

const CartContext = createContext<CartContextValue | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [cart, setCart] = useState<ApiCart | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Cart is per-user — reload on login, drop local state on logout.
  useEffect(() => {
    if (!user) {
      setCart(null);
      setError(null);
      return;
    }

    let cancelled = false;
    setLoading(true);
    fetchCart()
      .then((data) => {
        if (!cancelled) setCart(data);
      })
      .catch((err) => {
        if (!cancelled) setError(extractErrorMessage(err, "Couldn't load your cart."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [user]);

  const addItem = useCallback(async (menuItemId: number, quantity = 1) => {
    setError(null);
    try {
      setCart(await addCartItem(menuItemId, quantity));
    } catch (err) {
      const message = extractErrorMessage(err, "Couldn't add that to your cart.");
      setError(message);
      throw new Error(message);
    }
  }, []);

  const updateItem = useCallback(async (cartItemId: number, quantity: number) => {
    setError(null);
    try {
      setCart(await updateCartItem(cartItemId, quantity));
    } catch (err) {
      const message = extractErrorMessage(err, "Couldn't update that item.");
      setError(message);
      throw new Error(message);
    }
  }, []);

  const removeItem = useCallback(async (cartItemId: number) => {
    setError(null);
    try {
      setCart(await removeCartItem(cartItemId));
    } catch (err) {
      const message = extractErrorMessage(err, "Couldn't remove that item.");
      setError(message);
      throw new Error(message);
    }
  }, []);

  const clear = useCallback(async () => {
    setError(null);
    try {
      setCart(await clearCartRequest());
    } catch (err) {
      const message = extractErrorMessage(err, "Couldn't clear your cart.");
      setError(message);
      throw new Error(message);
    }
  }, []);

  const refresh = useCallback(async () => {
    if (!user) return;
    setError(null);
    try {
      setCart(await fetchCart());
    } catch (err) {
      setError(extractErrorMessage(err, "Couldn't refresh your cart."));
    }
  }, [user]);

  // See AuthContext's identical comment — unmemoized values here force every useCart()
  // consumer to re-render (and, for pages like OrderDetail, redundantly refetch) on every
  // CartProvider render, not just ones that actually changed cart/loading/error.
  const value = useMemo(
    () => ({ cart, loading, error, addItem, updateItem, removeItem, clear, refresh }),
    [cart, loading, error, addItem, updateItem, removeItem, clear, refresh]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart(): CartContextValue {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used within a CartProvider");
  return ctx;
}
