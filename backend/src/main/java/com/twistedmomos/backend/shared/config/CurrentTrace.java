package com.twistedmomos.backend.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the active trace ID for callers that build an ErrorResponse.
 *
 * <p>Exists so the three places that construct that envelope — the exception handler and the two
 * Spring Security entry points, which bypass it — do not each repeat the null-span handling.
 */
@Component
@RequiredArgsConstructor
public class CurrentTrace {

    private final Tracer tracer;

    /** Null when nothing is being traced, which leaves the field out of the JSON entirely. */
    public String traceId() {
        Span span = tracer.currentSpan();
        return span != null ? span.context().traceId() : null;
    }
}
