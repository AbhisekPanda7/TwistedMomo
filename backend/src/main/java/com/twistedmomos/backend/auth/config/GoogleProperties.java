package com.twistedmomos.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param clientId the OAuth web client id. Not a secret — it ships in the frontend bundle —
 *     but it is the audience every ID token is checked against.
 */
@ConfigurationProperties(prefix = "app.google")
public record GoogleProperties(String clientId) {}
