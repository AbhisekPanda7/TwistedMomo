package com.twistedmomos.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record AvailabilityRequest(
        @NotNull(message = "available is required")
        Boolean available
) {
}
