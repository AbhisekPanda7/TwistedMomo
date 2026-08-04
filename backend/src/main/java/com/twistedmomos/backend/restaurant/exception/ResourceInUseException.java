package com.twistedmomos.backend.restaurant.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

/** Thrown when a delete would orphan dependent rows (e.g. a category that still has menu items). */
public class ResourceInUseException extends DomainException {

    public ResourceInUseException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
