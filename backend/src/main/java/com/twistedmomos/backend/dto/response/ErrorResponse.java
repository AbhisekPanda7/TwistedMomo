package com.twistedmomos.backend.dto.response;

import java.time.Instant;
import java.util.List;

/** Single error envelope returned by every failure path in the API — see GlobalExceptionHandler. */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationErrorItem> validationErrors
) {
}
