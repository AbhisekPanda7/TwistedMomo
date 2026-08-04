package com.twistedmomos.backend.shared.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TraceResponseFilterTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @Test
    void setsTraceIdHeaderWhenASpanIsActive() throws Exception {
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext context = mock(TraceContext.class);
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        when(context.traceId()).thenReturn(TRACE_ID);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        new TraceResponseFilter(tracer).doFilter(new MockHttpServletRequest(), response, chain);

        assertThat(response.getHeader(TraceResponseFilter.TRACE_ID_HEADER)).isEqualTo(TRACE_ID);
    }

    /** No span means no header — a request must never fail because tracing is unavailable. */
    @Test
    void omitsHeaderAndStillProceedsWhenThereIsNoSpan() throws Exception {
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        new TraceResponseFilter(tracer).doFilter(request, response, chain);

        assertThat(response.getHeader(TraceResponseFilter.TRACE_ID_HEADER)).isNull();
        verify(chain).doFilter(request, response);
    }
}
