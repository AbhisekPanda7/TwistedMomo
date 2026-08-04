package com.twistedmomos.backend.auth.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
