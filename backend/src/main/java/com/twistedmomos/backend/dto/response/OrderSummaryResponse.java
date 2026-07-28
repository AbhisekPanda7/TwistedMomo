package com.twistedmomos.backend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
        Long id,
        String status,
        int totalItems,
        BigDecimal subtotal,
        String customerName,
        String customerEmail,
        Instant createdAt
) {
}
