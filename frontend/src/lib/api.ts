import axios from "axios";
import { clearSession, getAccessToken, getRefreshToken, storeSession } from "./tokenStorage";
import type { AuthSession } from "./tokenStorage";

const baseURL = import.meta.env.VITE_API_BASE_URL;

export const api = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

/** Separate instance for the refresh call itself, so it never re-enters api's own 401 handling below. */
const refreshClient = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.set("Authorization", `Bearer ${token}`);
  }
  return config;
});

let refreshInFlight: Promise<string | null> | null = null;

/** Single-flight refresh: concurrent 401s across multiple requests all await one in-progress refresh instead of racing. */
function refreshAccessToken(): Promise<string | null> {
  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      const refreshToken = getRefreshToken();
      if (!refreshToken) return null;
      try {
        const { data } = await refreshClient.post<AuthSession>("/auth/refresh", { refreshToken });
        storeSession(data);
        return data.accessToken;
      } catch {
        clearSession();
        window.dispatchEvent(new Event("auth:logout"));
        return null;
      } finally {
        refreshInFlight = null;
      }
    })();
  }
  return refreshInFlight;
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config as (typeof error.config & { _retried?: boolean }) | undefined;

    if (error.response?.status === 401 && original && !original._retried && getRefreshToken()) {
      original._retried = true;
      const newToken = await refreshAccessToken();
      if (newToken) {
        original.headers = original.headers ?? {};
        original.headers.Authorization = `Bearer ${newToken}`;
        return api(original);
      }
    }

    return Promise.reject(error);
  }
);
