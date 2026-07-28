package com.twistedmomos.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        int totalItems,
        BigDecimal subtotal
) {
}
