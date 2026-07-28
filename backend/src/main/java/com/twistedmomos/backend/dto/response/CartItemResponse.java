package com.twistedmomos.backend.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long menuItemId,
        String menuItemName,
        String menuItemImageUrl,
        BigDecimal unitPrice,
        boolean available,
        int quantity,
        BigDecimal lineTotal
) {
}
