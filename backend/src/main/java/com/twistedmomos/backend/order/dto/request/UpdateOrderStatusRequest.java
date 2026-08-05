package com.twistedmomos.backend.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Kept as a plain string (not a typed enum) rather than typed as OrderStatus, so an unrecognized value fails as a clean domain error from the service instead of a raw JSON deserialization 500 — same convention as MenuItemRequest.tag. */
public record UpdateOrderStatusRequest(
        @NotBlank(message = "status is required")
        String status,

        // Not @NotBlank — only a restaurant decline supplies one.
        @Size(max = 255, message = "reason must be 255 characters or fewer")
        String reason
) {
}
