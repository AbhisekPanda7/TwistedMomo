package com.twistedmomos.backend.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class TooManyAttemptsException extends DomainException {

    public TooManyAttemptsException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
