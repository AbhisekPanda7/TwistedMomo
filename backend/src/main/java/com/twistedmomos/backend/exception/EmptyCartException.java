package com.twistedmomos.backend.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class EmptyCartException extends DomainException {

    public EmptyCartException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
