package com.twistedmomos.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInRequest(
        @NotBlank(message = "idToken is required")
        String idToken
) {
}
