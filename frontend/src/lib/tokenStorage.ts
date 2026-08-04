export type AuthUser = {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  roles: string[];
  /** @deprecated highest-privilege role only — read `roles`. Goes away once no client reads it. */
  role?: string;
  createdAt: string;
};

/**
 * Tolerates a session stored before `roles` existed: that object survives a deploy in
 * localStorage, and `.includes` on the missing field would throw on first load.
 */
export function hasRole(user: AuthUser | null | undefined, role: string): boolean {
  if (!user) return false;
  return user.roles ? user.roles.includes(role) : user.role === role;
}

export type AuthSession = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: AuthUser;
};

const ACCESS_TOKEN_KEY = "tm_access_token";
const REFRESH_TOKEN_KEY = "tm_refresh_token";
const USER_KEY = "tm_user";

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function getStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function storeSession(session: AuthSession) {
  localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify(session.user));
}

export function clearSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}
