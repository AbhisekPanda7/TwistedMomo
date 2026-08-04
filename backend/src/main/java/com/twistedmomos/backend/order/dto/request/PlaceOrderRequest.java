package com.twistedmomos.backend.order.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Either addressId names a saved address, or the address fields are supplied inline.
 * The service enforces that one of the two is present — bean validation cannot express
 * "required unless another field is set".
 */
public record PlaceOrderRequest(
        @Size(max = 100, message = "recipientName must be at most 100 characters")
        String recipientName,

        @Size(max = 20, message = "phone must be at most 20 characters")
        String phone,

        @Size(max = 200, message = "addressLine1 must be at most 200 characters")
        String addressLine1,

        @Size(max = 200, message = "addressLine2 must be at most 200 characters")
        String addressLine2,

        @Size(max = 100, message = "city must be at most 100 characters")
        String city,

        @Size(max = 20, message = "postalCode must be at most 20 characters")
        String postalCode,

        @Size(max = 500, message = "notes must be at most 500 characters")
        String notes,

        /** A saved address to deliver to. When absent, the fields above are used. */
        Long addressId
) {
}
