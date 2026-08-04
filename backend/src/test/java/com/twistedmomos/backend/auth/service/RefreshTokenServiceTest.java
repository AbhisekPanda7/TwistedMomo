package com.twistedmomos.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.auth.entity.RefreshToken;
import com.twistedmomos.backend.auth.entity.Role;
import com.twistedmomos.backend.auth.entity.RoleName;
import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.auth.exception.InvalidRefreshTokenException;
import com.twistedmomos.backend.auth.repository.RefreshTokenRepository;
import com.twistedmomos.backend.auth.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenSecurityActions securityActions;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name(RoleName.CUSTOMER).build();
        user = User.builder().id(1L).name("Token Tester").email("token.tester@example.com").role(role).build();
    }

    @Test
    void rotate_withAValidToken_revokesItAndIssuesAFreshOne() {
        RefreshToken existing = RefreshToken.builder()
                .id(1L).user(user).token("old-token").revoked(false)
                .expiresAt(Instant.now().plusSeconds(3600)).createdAt(Instant.now())
                .build();
        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(2_592_000_000L);

        RefreshToken rotated = refreshTokenService.rotate("old-token");

        assertThat(existing.isRevoked()).isTrue();
        assertThat(rotated.getToken()).isNotEqualTo("old-token");
        assertThat(rotated.isRevoked()).isFalse();
        assertThat(rotated.getUser()).isEqualTo(user);
        verify(securityActions, never()).revokeAllActiveForUser(any());
    }

    @Test
    void rotate_withAnAlreadyRevokedToken_revokesEveryTokenForThatUserAndRejects() {
        RefreshToken alreadyRevoked = RefreshToken.builder()
                .id(1L).user(user).token("stolen-token").revoked(true)
                .expiresAt(Instant.now().plusSeconds(3600)).createdAt(Instant.now())
                .build();
        when(refreshTokenRepository.findByToken("stolen-token")).thenReturn(Optional.of(alreadyRevoked));

        assertThatThrownBy(() -> refreshTokenService.rotate("stolen-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(securityActions).revokeAllActiveForUser(eq(user.getId()));
    }

    @Test
    void rotate_withAnExpiredToken_rejectsWithoutTouchingOtherSessions() {
        RefreshToken expired = RefreshToken.builder()
                .id(1L).user(user).token("expired-token").revoked(false)
                .expiresAt(Instant.now().minusSeconds(10)).createdAt(Instant.now())
                .build();
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.rotate("expired-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(securityActions, never()).revokeAllActiveForUser(any());
    }

    @Test
    void rotate_withAnUnrecognizedToken_rejects() {
        when(refreshTokenRepository.findByToken("no-such-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotate("no-such-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void revoke_marksTheMatchingTokenRevoked() {
        RefreshToken existing = RefreshToken.builder()
                .id(1L).user(user).token("logout-token").revoked(false)
                .expiresAt(Instant.now().plusSeconds(3600)).createdAt(Instant.now())
                .build();
        when(refreshTokenRepository.findByToken("logout-token")).thenReturn(Optional.of(existing));

        refreshTokenService.revoke("logout-token");

        assertThat(existing.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(existing);
    }
}
