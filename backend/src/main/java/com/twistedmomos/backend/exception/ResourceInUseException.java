package com.twistedmomos.backend.exception;

/** Thrown when a delete would orphan dependent rows (e.g. a category that still has menu items). */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
