package com.twistedmomos.backend.auth.dto.response;

import java.time.Instant;

public record AddressResponse(
        Long id,
        String recipientName,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String postalCode,
        Instant lastUsedAt) {}
