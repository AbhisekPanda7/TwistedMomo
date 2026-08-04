package com.twistedmomos.backend.auth.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidVerificationTokenException extends DomainException {

    public InvalidVerificationTokenException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
