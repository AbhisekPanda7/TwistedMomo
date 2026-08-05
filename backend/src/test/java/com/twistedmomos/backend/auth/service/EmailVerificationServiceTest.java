package com.twistedmomos.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.auth.config.AuthLinkProperties;
import com.twistedmomos.backend.auth.entity.EmailVerificationToken;
import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.auth.event.VerificationRequestedEvent;
import com.twistedmomos.backend.auth.exception.InvalidVerificationTokenException;
import com.twistedmomos.backend.auth.repository.EmailVerificationTokenRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher events;
    @Mock private AuthLinkProperties linkProperties;

    @InjectMocks private EmailVerificationService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Verify Tester").email("verify@example.com").build();
    }

    /**
     * The stored hash must not be the token itself — a leaked table would otherwise be a
     * set of working verification links.
     */
    @Test
    void storesOnlyAHashOfTheEmailedToken() {
        when(linkProperties.frontendBaseUrl()).thenReturn("https://twistedmomos.tech");

        service.sendVerificationLink(user);

        ArgumentCaptor<EmailVerificationToken> saved = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(saved.capture());
        ArgumentCaptor<VerificationRequestedEvent> published =
                ArgumentCaptor.forClass(VerificationRequestedEvent.class);
        verify(events).publishEvent(published.capture());

        String hash = saved.getValue().getTokenHash();
        assertThat(hash).hasSize(64);
        assertThat(published.getValue().link()).doesNotContain(hash);
    }

    /** Delivery is another module's job — this event is everything it needs, values only. */
    @Test
    void publishesTheLinkAddressedToTheUser() {
        when(linkProperties.frontendBaseUrl()).thenReturn("https://twistedmomos.tech");

        service.sendVerificationLink(user);

        ArgumentCaptor<VerificationRequestedEvent> published =
                ArgumentCaptor.forClass(VerificationRequestedEvent.class);
        verify(events).publishEvent(published.capture());
        assertThat(published.getValue().userId()).isEqualTo(1L);
        assertThat(published.getValue().email()).isEqualTo("verify@example.com");
        assertThat(published.getValue().name()).isEqualTo("Verify Tester");
        assertThat(published.getValue().link())
                .startsWith("https://twistedmomos.tech/verify-email?token=");
    }

    /** Issuing a new link must retire the previous one, so a resend cannot leave two live tokens. */
    @Test
    void invalidatesOutstandingTokensBeforeIssuingAnother() {
        when(linkProperties.frontendBaseUrl()).thenReturn("https://twistedmomos.tech");

        service.sendVerificationLink(user);

        verify(tokenRepository).invalidateOutstanding(anyLong(), any(Instant.class));
    }

    @Test
    void sendsNothingWhenAlreadyVerified() {
        user.setEmailVerified(true);

        service.sendVerificationLink(user);

        verify(tokenRepository, never()).save(any());
        verify(events, never()).publishEvent(any());
    }

    @Test
    void throttlesAfterTooManyRequestsInTheWindow() {
        when(tokenRepository.countByUserIdAndCreatedAtAfter(anyLong(), any(Instant.class))).thenReturn(5L);

        service.sendVerificationLink(user);

        verify(tokenRepository, never()).save(any());
        verify(events, never()).publishEvent(any());
    }

    @Test
    void marksTheUserVerifiedAndConsumesTheToken() {
        EmailVerificationToken token = usableToken();
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(java.util.Optional.of(token));

        service.verify("some-token");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void rejectsAnAlreadyUsedToken() {
        EmailVerificationToken token = usableToken();
        token.setUsedAt(Instant.now().minus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(java.util.Optional.of(token));

        assertThatThrownBy(() -> service.verify("some-token"))
                .isInstanceOf(InvalidVerificationTokenException.class);
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void rejectsAnExpiredToken() {
        EmailVerificationToken token = usableToken();
        token.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(java.util.Optional.of(token));

        assertThatThrownBy(() -> service.verify("some-token"))
                .isInstanceOf(InvalidVerificationTokenException.class);
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void rejectsAnUnknownToken() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.verify("nonsense"))
                .isInstanceOf(InvalidVerificationTokenException.class);
    }

    private EmailVerificationToken usableToken() {
        return EmailVerificationToken.builder()
                .id(1L)
                .user(user)
                .tokenHash("hash")
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
    }
}
