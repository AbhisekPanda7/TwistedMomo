package com.twistedmomos.backend.auth.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidRoleException extends DomainException {

    public InvalidRoleException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
