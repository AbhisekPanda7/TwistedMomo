package com.twistedmomos.backend.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.shared.exception.ResourceNotFoundException;
import com.twistedmomos.backend.shared.config.CurrentTrace;
import com.twistedmomos.backend.shared.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @Test
    void carriesTheActiveTraceIdIntoTheErrorEnvelope() {
        CurrentTrace currentTrace = mock(CurrentTrace.class);
        when(currentTrace.traceId()).thenReturn(TRACE_ID);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/orders/42");

        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler(currentTrace)
                .handleDomain(new ResourceNotFoundException("Order not found"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().traceId()).isEqualTo(TRACE_ID);
        assertThat(response.getBody().path()).isEqualTo("/api/v1/orders/42");
    }

    /** Absent rather than the string "null" — Jackson drops the field, so clients see no reference at all. */
    @Test
    void leavesTraceIdNullWhenNothingIsBeingTraced() {
        CurrentTrace currentTrace = mock(CurrentTrace.class);
        when(currentTrace.traceId()).thenReturn(null);

        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler(currentTrace)
                .handleDomain(new ResourceNotFoundException("Order not found"), new MockHttpServletRequest());

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().traceId()).isNull();
    }
}
