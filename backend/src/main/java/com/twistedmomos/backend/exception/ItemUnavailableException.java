package com.twistedmomos.backend.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class ItemUnavailableException extends DomainException {

    public ItemUnavailableException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
