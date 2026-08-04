package com.twistedmomos.backend.order.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidOrderStatusTransitionException extends DomainException {

    public InvalidOrderStatusTransitionException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
