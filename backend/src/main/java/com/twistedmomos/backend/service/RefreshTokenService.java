package com.twistedmomos.backend.service;

import com.twistedmomos.backend.entity.RefreshToken;
import com.twistedmomos.backend.entity.User;
import com.twistedmomos.backend.exception.InvalidRefreshTokenException;
import com.twistedmomos.backend.repository.RefreshTokenRepository;
import com.twistedmomos.backend.security.JwtService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return refreshTokenRepository.save(refreshToken);
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
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not recognized"));

        if (token.isRevoked()) {
            // Must commit independently of this method's own transaction — see
            // RefreshTokenSecurityActions' javadoc for why.
            securityActions.revokeAllActiveForUser(token.getUser().getId());
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        return create(token.getUser());
    }

    @Transactional
    public void revoke(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }
}
