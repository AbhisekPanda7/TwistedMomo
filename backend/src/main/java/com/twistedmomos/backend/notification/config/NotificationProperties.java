package com.twistedmomos.backend.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param apiKey    Resend key. Absent in dev, where the logging adapter takes over.
 * @param fromEmail sender address; its domain must be verified with Resend
 */
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(String apiKey, String fromName, String fromEmail) {}
