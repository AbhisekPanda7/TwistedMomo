package com.twistedmomos.backend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String status,
        List<OrderItemResponse> items,
        int totalItems,
        BigDecimal subtotal,
        String recipientName,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String postalCode,
        String notes,
        String customerName,
        String customerEmail,
        Instant createdAt,
        Instant updatedAt
) {
}
