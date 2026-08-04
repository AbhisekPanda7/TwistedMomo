package com.twistedmomos.backend.auth.security;

import com.twistedmomos.backend.auth.exception.TooManyAttemptsException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * In-memory brute-force guard on /auth/login, keyed by email. Deliberately not
 * distributed (Redis/etc.) — this app runs as a single Render instance, and a
 * per-instance limiter is sufficient there. If this ever scales to multiple
 * instances, swap the backing map for a shared store; the tracking logic here
 * would stay the same.
 */
@Slf4j
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCKOUT = Duration.ofMinutes(15);

    private final Map<String, Attempts> attemptsByEmail = new ConcurrentHashMap<>();

    private record Attempts(int count, Instant windowStart, Instant lockedUntil) {
    }

    public void checkAllowed(String email) {
        Attempts attempts = attemptsByEmail.get(normalize(email));
        if (attempts != null && attempts.lockedUntil() != null && attempts.lockedUntil().isAfter(Instant.now())) {
            // No email in the message — the count and remaining lockout are what an
            // operator needs to spot a brute-force attempt in progress.
            log.warn("Login blocked — account locked out: attempts={} remainingSeconds={}",
                    attempts.count(), Duration.between(Instant.now(), attempts.lockedUntil()).toSeconds());
            throw new TooManyAttemptsException("Too many failed login attempts. Please try again later.");
        }
    }

    public void recordFailure(String email) {
        Attempts updated = attemptsByEmail.compute(normalize(email), (key, existing) -> {
            Instant now = Instant.now();
            if (existing == null || existing.windowStart().plus(WINDOW).isBefore(now)) {
                return new Attempts(1, now, null);
            }
            int count = existing.count() + 1;
            Instant lockedUntil = count >= MAX_ATTEMPTS ? now.plus(LOCKOUT) : null;
            return new Attempts(count, existing.windowStart(), lockedUntil);
        });

        if (updated.lockedUntil() != null) {
            log.warn("Account locked out after repeated failures: attempts={} lockoutMinutes={}",
                    updated.count(), LOCKOUT.toMinutes());
        } else {
            log.warn("Failed login recorded: attempts={} maxAttempts={}", updated.count(), MAX_ATTEMPTS);
        }
    }

    public void recordSuccess(String email) {
        attemptsByEmail.remove(normalize(email));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
