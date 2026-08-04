package com.twistedmomos.backend.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(
        @NotBlank(message = "recipientName is required")
        @Size(max = 100, message = "recipientName must be at most 100 characters")
        String recipientName,

        @NotBlank(message = "phone is required")
        @Size(max = 20, message = "phone must be at most 20 characters")
        String phone,

        @NotBlank(message = "addressLine1 is required")
        @Size(max = 200, message = "addressLine1 must be at most 200 characters")
        String addressLine1,

        @Size(max = 200, message = "addressLine2 must be at most 200 characters")
        String addressLine2,

        @NotBlank(message = "city is required")
        @Size(max = 100, message = "city must be at most 100 characters")
        String city,

        @NotBlank(message = "postalCode is required")
        @Size(max = 20, message = "postalCode must be at most 20 characters")
        String postalCode,

        @Size(max = 500, message = "notes must be at most 500 characters")
        String notes
) {
}
