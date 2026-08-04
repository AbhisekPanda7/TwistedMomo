package com.twistedmomos.backend.auth.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

/** 409: the address is spoken for by an account that has not proved it owns it. */
public class UnverifiedLocalAccountException extends DomainException {

    public UnverifiedLocalAccountException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
