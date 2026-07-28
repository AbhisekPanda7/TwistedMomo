package com.twistedmomos.backend.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long menuItemId,
        String menuItemName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
