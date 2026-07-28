package com.twistedmomos.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Kept as a plain string (not a typed enum) rather than typed as OrderStatus, so an unrecognized value fails as a clean domain error from the service instead of a raw JSON deserialization 500 — same convention as MenuItemRequest.tag. */
public record UpdateOrderStatusRequest(
        @NotBlank(message = "status is required")
        String status
) {
}
