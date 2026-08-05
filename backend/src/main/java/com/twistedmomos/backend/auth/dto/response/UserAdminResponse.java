package com.twistedmomos.backend.auth.dto.response;

import java.time.Instant;
import java.util.Set;

public record UserAdminResponse(
        Long id,
        String name,
        String email,
        String phone,
        Set<String> roles,
        boolean enabled,
        boolean emailVerified,
        Instant createdAt) {}
