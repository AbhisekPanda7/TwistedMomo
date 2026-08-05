package com.twistedmomos.backend.notification.dto.response;

import java.time.Instant;

public record NotificationResponse(
        Long id, String type, String title, String body, Long orderId,
        boolean read, Instant createdAt) {}
