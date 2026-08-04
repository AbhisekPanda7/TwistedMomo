package com.twistedmomos.backend.auth.dto.response;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        /** Highest-privilege role. Kept so existing clients keep working — prefer {@code roles}. */
        String role,
        List<String> roles,
        boolean emailVerified,
        Instant createdAt
) {
}
