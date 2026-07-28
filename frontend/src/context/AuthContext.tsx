import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api } from "../lib/api";
import {
  clearSession,
  getRefreshToken,
  getStoredUser,
  storeSession,
  type AuthSession,
  type AuthUser,
} from "../lib/tokenStorage";

type RegisterPayload = { name: string; email: string; password: string; phone?: string };
type LoginPayload = { email: string; password: string };

type AuthContextValue = {
  user: AuthUser | null;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => getStoredUser());

  // Fired by the api.ts response interceptor when a refresh attempt fails outright.
  useEffect(() => {
    function handleForcedLogout() {
      setUser(null);
    }
    window.addEventListener("auth:logout", handleForcedLogout);
    return () => window.removeEventListener("auth:logout", handleForcedLogout);
  }, []);

  const login = useCallback(async (payload: LoginPayload) => {
    const { data } = await api.post<AuthSession>("/auth/login", payload);
    storeSession(data);
    setUser(data.user);
  }, []);

  const register = useCallback(async (payload: RegisterPayload) => {
    const { data } = await api.post<AuthSession>("/auth/register", payload);
    storeSession(data);
    setUser(data.user);
  }, []);

  const logout = useCallback(() => {
    const refreshToken = getRefreshToken();
    clearSession();
    setUser(null);
    if (refreshToken) {
      api.post("/auth/logout", { refreshToken }).catch(() => {
        // best-effort server-side revoke — local session is already cleared either way
      });
    }
  }, []);

  // Without this, every AuthProvider render (including ones triggered by unrelated
  // ancestors) hands consumers a brand-new object, forcing every useAuth() caller in the
  // tree to re-render — including "zombie" instances Framer Motion keeps mounted during
  // exit animations, which is exactly what caused the redirect race ProtectedRoute now
  // guards against.
  const value = useMemo(() => ({ user, login, register, logout }), [user, login, register, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
