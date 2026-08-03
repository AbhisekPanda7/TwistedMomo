package com.twistedmomos.backend.service;

import com.twistedmomos.backend.entity.RefreshToken;
import com.twistedmomos.backend.entity.User;
import com.twistedmomos.backend.exception.InvalidRefreshTokenException;
import com.twistedmomos.backend.repository.RefreshTokenRepository;
import com.twistedmomos.backend.security.JwtService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Token values are never logged — a refresh token is a bearer credential, so a log
// line containing one is as good as the credential itself.
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final RefreshTokenSecurityActions securityActions;

    @Transactional
    public RefreshToken create(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString() + UUID.randomUUID())
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token issued: userId={} expiresAt={}", user.getId(), saved.getExpiresAt());
        return saved;
    }

    /**
     * Rotates a refresh token: the presented token is revoked and a brand-new one takes
     * its place. If the presented token was ALREADY revoked, that's a strong signal it was
     * stolen and replayed after the legitimate client already rotated past it — every
     * refresh token this user holds is revoked in response, forcing all of their sessions
     * to re-authenticate. This is the standard "rotation with reuse detection" pattern.
     */
    @Transactional
    public RefreshToken rotate(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> {
                    log.warn("Refresh rejected — token not recognized");
                    return new InvalidRefreshTokenException("Refresh token not recognized");
                });

        Long userId = token.getUser().getId();

        if (token.isRevoked()) {
            // Must commit independently of this method's own transaction — see
            // RefreshTokenSecurityActions' javadoc for why.
            log.warn("Refresh token reuse detected — revoking all sessions: userId={}", userId);
            securityActions.revokeAllActiveForUser(userId);
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Refresh rejected — token expired: userId={} expiredAt={}", userId, token.getExpiresAt());
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("Refresh token rotated: userId={}", userId);
        return create(token.getUser());
    }

    @Transactional
    public void revoke(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue)
                .ifPresentOrElse(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("Refresh token revoked: userId={}", token.getUser().getId());
                }, () -> log.warn("Revoke request for an unrecognized refresh token"));
    }
}
