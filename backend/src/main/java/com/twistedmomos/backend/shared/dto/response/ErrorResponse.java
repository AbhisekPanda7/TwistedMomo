package com.twistedmomos.backend.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Single error envelope returned by every failure path in the API — see GlobalExceptionHandler.
 *
 * <p>traceId matches the X-Trace-Id response header and the traceId on every log line for the
 * request, so a reference quoted by a user resolves straight to that request's logs.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId,
        List<ValidationErrorItem> validationErrors
) {
}
