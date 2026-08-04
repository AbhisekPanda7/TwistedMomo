package com.twistedmomos.backend.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base for expected module failures. The status lives on the exception so the handler
 * dispatches on this type alone — otherwise shared would have to import every module.
 */
public abstract class DomainException extends RuntimeException {

    private final HttpStatus status;

    protected DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
