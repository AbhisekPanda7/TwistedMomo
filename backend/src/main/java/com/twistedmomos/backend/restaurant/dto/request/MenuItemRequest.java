package com.twistedmomos.backend.restaurant.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record MenuItemRequest(
        @NotNull(message = "categoryId is required")
        Long categoryId,

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 160, message = "Slug must be at most 160 characters")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug must be lowercase kebab-case, e.g. chicken-steam-momos")
        String slug,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "veg is required")
        Boolean veg,

        @Min(value = 1, message = "spicyLevel must be between 1 and 3")
        @Max(value = 3, message = "spicyLevel must be between 1 and 3")
        Integer spicyLevel,

        @Pattern(regexp = "^(BESTSELLER|SIGNATURE|NEW)$", message = "tag must be one of BESTSELLER, SIGNATURE, NEW")
        String tag,

        Boolean available,

        Integer displayOrder
) {
}
