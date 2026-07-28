package com.twistedmomos.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * secret is intentionally required with no default in application.yml for
 * the prod profile — see application-prod.yml.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs
) {
}
