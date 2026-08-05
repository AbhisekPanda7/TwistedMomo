package com.twistedmomos.backend.auth.event;

/**
 * Published after a verification link is issued and stored. Carries the rendered link and
 * greeting name rather than the raw token or a {@code User}: notification only needs to
 * address and deliver an email, never to know how the token was generated or hashed.
 */
public record VerificationRequestedEvent(Long userId, String email, String name, String link) {}
