package com.twistedmomos.backend.auth.dto.response;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String role,
        Instant createdAt
) {
}
