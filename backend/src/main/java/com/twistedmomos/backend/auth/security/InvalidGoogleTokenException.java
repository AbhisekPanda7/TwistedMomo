package com.twistedmomos.backend.auth.security;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidGoogleTokenException extends DomainException {

    public InvalidGoogleTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
