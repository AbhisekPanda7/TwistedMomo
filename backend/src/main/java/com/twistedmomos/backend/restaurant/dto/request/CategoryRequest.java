package com.twistedmomos.backend.restaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 100, message = "Slug must be at most 100 characters")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug must be lowercase kebab-case, e.g. chilli-butter-garlic")
        String slug,

        @Size(max = 255, message = "Description must be at most 255 characters")
        String description,

        Integer displayOrder,

        Boolean active
) {
}
