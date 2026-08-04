package com.twistedmomos.backend.dto.response;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean veg,
        Integer spicyLevel,
        String tag,
        boolean available,
        int displayOrder,
        Long categoryId,
        String categoryName,
        String categorySlug
) {
}
