package com.twistedmomos.backend.auth.security;

import com.twistedmomos.backend.auth.config.GoogleProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import java.security.Key;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Verifies a Google ID token and reports who it belongs to.
 *
 * <p>Every check here is load-bearing. Skipping the audience check would accept a token
 * minted for a different application; skipping the issuer check would accept one from
 * anywhere. jjwt enforces signature and expiry, the rest is explicit below.
 */
@Slf4j
@Component
public class GoogleIdTokenVerifier {

    private static final String JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> VALID_ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");
    /** Google rotates signing keys roughly daily; this only bounds how stale a cached set can get. */
    private static final Duration KEY_CACHE_TTL = Duration.ofHours(1);

    private final RestClient client = RestClient.create();
    private final GoogleProperties properties;

    private volatile JwkSet cachedKeys;
    private volatile Instant cachedAt = Instant.EPOCH;

    public GoogleIdTokenVerifier(GoogleProperties properties) {
        this.properties = properties;
    }

    /**
     * @throws InvalidGoogleTokenException if the token is unusable for any reason — a bad
     *     signature, the wrong audience or issuer, expiry, or an unverified address
     */
    public GoogleIdentity verify(String idToken) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .keyLocator(this::locate)
                    .requireIssuer(null) // checked below against both accepted spellings
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Google sign-in rejected — token did not verify: {}", ex.getClass().getSimpleName());
            throw new InvalidGoogleTokenException("Could not verify that Google account");
        }

        if (!VALID_ISSUERS.contains(claims.getIssuer())) {
            log.warn("Google sign-in rejected — unexpected issuer");
            throw new InvalidGoogleTokenException("Could not verify that Google account");
        }
        if (!properties.clientId().equals(claims.getAudience() == null ? null : claims.getAudience().iterator().next())) {
            // A token minted for another application would otherwise be accepted here.
            log.warn("Google sign-in rejected — token was issued for a different application");
            throw new InvalidGoogleTokenException("Could not verify that Google account");
        }

        String email = claims.get("email", String.class);
        Boolean emailVerified = claims.get("email_verified", Boolean.class);
        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            // Without this an attacker could register an unverified Google identity on
            // someone else's address and inherit their account through the link rule.
            log.warn("Google sign-in rejected — Google has not verified that address");
            throw new InvalidGoogleTokenException("Google has not confirmed that email address");
        }

        return new GoogleIdentity(claims.getSubject(), email, claims.get("name", String.class));
    }

    /** Resolves the signing key by {@code kid}, refetching the set when the cache is stale. */
    private Key locate(io.jsonwebtoken.Header header) {
        String keyId = (String) header.get("kid");
        JwkSet keys = keys(false);
        PublicKey found = findKey(keys, keyId);
        if (found == null) {
            // Unknown kid usually means Google rotated — one forced refetch before giving up.
            found = findKey(keys(true), keyId);
        }
        if (found == null) {
            throw new InvalidGoogleTokenException("Could not verify that Google account");
        }
        return found;
    }

    private PublicKey findKey(JwkSet keys, String keyId) {
        for (Jwk<?> jwk : keys) {
            if (keyId != null && keyId.equals(jwk.getId()) && jwk.toKey() instanceof PublicKey publicKey) {
                return publicKey;
            }
        }
        return null;
    }

    private JwkSet keys(boolean force) {
        JwkSet current = cachedKeys;
        if (!force && current != null && cachedAt.plus(KEY_CACHE_TTL).isAfter(Instant.now())) {
            return current;
        }
        String body = client.get().uri(JWKS_URL).retrieve().body(String.class);
        JwkSet fetched = Jwks.setParser().build().parse(body);
        cachedKeys = fetched;
        cachedAt = Instant.now();
        log.info("Google signing keys refreshed");
        return fetched;
    }
}
