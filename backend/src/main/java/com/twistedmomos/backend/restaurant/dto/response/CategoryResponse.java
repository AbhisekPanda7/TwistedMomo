package com.twistedmomos.backend.restaurant.dto.response;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        int displayOrder,
        boolean active
) {
}
