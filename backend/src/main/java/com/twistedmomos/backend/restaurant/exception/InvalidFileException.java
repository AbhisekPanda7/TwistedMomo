package com.twistedmomos.backend.restaurant.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidFileException extends DomainException {

    public InvalidFileException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
