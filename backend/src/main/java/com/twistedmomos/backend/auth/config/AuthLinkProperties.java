package com.twistedmomos.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param frontendBaseUrl public site origin — verification links point at the SPA, not the API
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthLinkProperties(String frontendBaseUrl) {}
