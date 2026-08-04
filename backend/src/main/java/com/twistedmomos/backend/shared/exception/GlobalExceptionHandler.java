package com.twistedmomos.backend.shared.exception;

import com.twistedmomos.backend.shared.config.CurrentTrace;
import com.twistedmomos.backend.shared.dto.response.ErrorResponse;
import com.twistedmomos.backend.shared.dto.response.ValidationErrorItem;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Every failure path in the API — validation, domain, auth, or unexpected — resolves to one ErrorResponse shape. */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final CurrentTrace currentTrace;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationErrorItem> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationErrorItem(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    /** One method for every module's failures — each exception carries its own status. */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getMessage(), request, null);
    }

    /** Covers BadCredentialsException, DisabledException, UsernameNotFoundException, etc. — one 401 shape for all of them. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", request, null);
    }

    /**
     * An unmapped path is a client mistake, not a server fault. Without this it falls to
     * the catch-all below and answers 500, which reads as "we are broken" and buries real
     * faults in the error logs.
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoResource(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "The requested resource was not found", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request, List<ValidationErrorItem> validationErrors) {
        // Every failure passes through here, so one statement covers them all. 4xx are
        // expected outcomes rather than faults — WARN, and no stack trace. The 5xx
        // catch-all logs its own ERROR with the exception above.
        if (status.is4xxClientError()) {
            log.warn("Request failed: status={} method={} path={} reason={}",
                    status.value(), request.getMethod(), request.getRequestURI(), message);
        }
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                currentTrace.traceId(),
                validationErrors);
        return ResponseEntity.status(status).body(body);
    }
}
