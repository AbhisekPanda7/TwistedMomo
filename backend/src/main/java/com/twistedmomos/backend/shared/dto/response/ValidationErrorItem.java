package com.twistedmomos.backend.shared.dto.response;

public record ValidationErrorItem(
        String field,
        String message
) {
}
