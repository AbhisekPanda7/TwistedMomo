/**
 * W3C Trace Context IDs, generated per request so a failure the user reports can be
 * found in the backend logs by ID rather than by guessing at timestamps.
 *
 * The backend adopts the trace ID off this header rather than minting its own, so the
 * value sent here is the one that ends up on every log line for the request.
 */

const randomHex = (bytes: number) =>
  Array.from(crypto.getRandomValues(new Uint8Array(bytes)))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");

/**
 * Builds a `traceparent` header value: version-traceId-spanId-flags.
 * The trailing `01` marks the trace as sampled; the backend records every request.
 */
export function newTraceparent(): string {
  return `00-${randomHex(16)}-${randomHex(8)}-01`;
}

/** Pulls the 32-hex trace ID out of a traceparent, or null if it is malformed. */
export function traceIdOf(traceparent: string | undefined): string | null {
  const traceId = traceparent?.split("-")[1];
  return traceId && /^[0-9a-f]{32}$/.test(traceId) ? traceId : null;
}
