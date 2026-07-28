package com.twistedmomos.backend.dto.response;

public record ValidationErrorItem(
        String field,
        String message
) {
}
