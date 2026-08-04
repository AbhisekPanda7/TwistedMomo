package com.twistedmomos.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Used for both POST /auth/refresh and POST /auth/logout — same shape, one token in, one action out. */
public record RefreshRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {
}
