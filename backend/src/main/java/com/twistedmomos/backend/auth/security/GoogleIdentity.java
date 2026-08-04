package com.twistedmomos.backend.auth.security;

/**
 * @param subject Google's stable user id — the thing we key an identity on. Email can change.
 */
public record GoogleIdentity(String subject, String email, String name) {}
