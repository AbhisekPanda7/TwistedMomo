package com.twistedmomos.backend.auth.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

/** The system must always retain at least one ADMIN, or nobody can grant roles again. */
public class LastAdminException extends DomainException {

    public LastAdminException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
