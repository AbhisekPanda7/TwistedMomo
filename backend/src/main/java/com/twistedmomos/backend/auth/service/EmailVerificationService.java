package com.twistedmomos.backend.auth.service;

import com.twistedmomos.backend.auth.config.AuthLinkProperties;
import com.twistedmomos.backend.auth.entity.EmailVerificationToken;
import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.auth.event.VerificationRequestedEvent;
import com.twistedmomos.backend.auth.exception.InvalidVerificationTokenException;
import com.twistedmomos.backend.auth.repository.EmailVerificationTokenRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final Duration RESEND_WINDOW = Duration.ofHours(1);
    private static final int MAX_SENDS_PER_WINDOW = 5;
    private static final int TOKEN_BYTES = 32;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher events;
    private final AuthLinkProperties linkProperties;
    private final SecureRandom random = new SecureRandom();

    /**
     * Issues a link and emails it. Silent when the user is already verified or has asked
     * too often — callers must not learn either fact, since both leak whether an address
     * is registered.
     */
    @Transactional
    public void sendVerificationLink(User user) {
        if (user.isEmailVerified()) {
            return;
        }
        Instant now = Instant.now();
        if (tokenRepository.countByUserIdAndCreatedAtAfter(user.getId(), now.minus(RESEND_WINDOW))
                >= MAX_SENDS_PER_WINDOW) {
            log.warn("Verification resend throttled: userId={}", user.getId());
            return;
        }

        tokenRepository.invalidateOutstanding(user.getId(), now);

        byte[] raw = new byte[TOKEN_BYTES];
        random.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        tokenRepository.save(EmailVerificationToken.builder()
                .user(user)
                .tokenHash(hash(token))
                .expiresAt(now.plus(TOKEN_TTL))
                .build());

        String link = "%s/verify-email?token=%s".formatted(linkProperties.frontendBaseUrl(), token);

        // Published inside the transaction so Modulith writes it to event_publication with
        // the token — a crash before the listener runs replays it instead of losing the email.
        events.publishEvent(new VerificationRequestedEvent(user.getId(), user.getEmail(), user.getName(), link));
        log.info("Verification link issued: userId={}", user.getId());
    }

    /** Consumes the token and marks the address verified. Single use, so a replay fails. */
    @Transactional
    public void verify(String token) {
        EmailVerificationToken stored = tokenRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> {
                    log.warn("Verification rejected — token not recognized");
                    return new InvalidVerificationTokenException("This verification link is not valid");
                });

        if (!stored.isUsable(Instant.now())) {
            log.warn("Verification rejected — token expired or already used: userId={}",
                    stored.getUser().getId());
            throw new InvalidVerificationTokenException("This verification link has expired or was already used");
        }

        stored.setUsedAt(Instant.now());
        User user = stored.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified: userId={}", user.getId());
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the JRE spec", e);
        }
    }
}
