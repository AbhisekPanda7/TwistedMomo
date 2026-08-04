package com.twistedmomos.backend.order.exception;

import com.twistedmomos.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

/** 400: the request named no saved address and supplied no fields to deliver to. */
public class MissingDeliveryAddressException extends DomainException {

    public MissingDeliveryAddressException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
