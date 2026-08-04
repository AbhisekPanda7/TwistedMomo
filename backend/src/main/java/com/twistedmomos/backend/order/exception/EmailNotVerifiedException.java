package com.twistedmomos.backend.order.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

/** 403 rather than 400: the request is well formed, the account just is not allowed to order yet. */
public class EmailNotVerifiedException extends DomainException {

    public EmailNotVerifiedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
