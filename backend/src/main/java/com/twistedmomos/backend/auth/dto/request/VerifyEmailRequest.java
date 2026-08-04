package com.twistedmomos.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "token is required")
        String token
) {
}
