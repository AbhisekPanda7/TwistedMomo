package com.twistedmomos.backend.auth.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class RoleNotGrantedException extends DomainException {

    public RoleNotGrantedException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
