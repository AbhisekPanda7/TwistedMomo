import axios from "axios";
import { traceIdOf } from "./trace";

/** A failed request reduced to what the UI needs: what to say, and how to look it up. */
export type ApiError = {
  message: string;
  /** Trace ID for this request, matching the backend logs. Absent if it could not be determined. */
  traceId: string | null;
  /** HTTP status, or null when the request never got a response (network failure, CORS, timeout). */
  status: number | null;
};

export function toApiError(err: unknown, fallback: string): ApiError {
  if (!axios.isAxiosError(err)) {
    return { message: fallback, traceId: null, status: null };
  }

  const data = err.response?.data as { message?: unknown; traceId?: unknown } | undefined;
  const headerTraceId = err.response?.headers?.["x-trace-id"];

  return {
    message: typeof data?.message === "string" ? data.message : fallback,
    // The error envelope is the most reliable source; the response header covers
    // failures rendered outside it; the outgoing header is the last resort, and the
    // only one available when the request never reached the backend at all.
    traceId:
      (typeof data?.traceId === "string" ? data.traceId : null) ??
      (typeof headerTraceId === "string" ? headerTraceId : null) ??
      traceIdOf(err.config?.headers?.traceparent as string | undefined),
    status: err.response?.status ?? null,
  };
}

/**
 * Whether to show the user a support reference.
 *
 * Only for failures they cannot act on themselves — a server fault or an unreachable
 * API. Quoting a trace ID next to "Invalid email or password" is noise.
 */
export function showTraceRef(error: ApiError): boolean {
  return error.traceId !== null && (error.status === null || error.status >= 500);
}

/** For client-side validation failures, which never reached the API and so have no trace. */
export function localError(message: string): ApiError {
  return { message, traceId: null, status: null };
}
