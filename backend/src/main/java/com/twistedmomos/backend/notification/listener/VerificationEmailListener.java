package com.twistedmomos.backend.notification.listener;

import com.twistedmomos.backend.auth.event.VerificationRequestedEvent;
import com.twistedmomos.backend.notification.VerificationEmailTemplate;
import com.twistedmomos.backend.notification.api.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Renders and sends the verification email.
 *
 * <p>{@code @ApplicationModuleListener} is async, {@code REQUIRES_NEW} and
 * {@code AFTER_COMMIT} together: the token is already stored by the time this runs, so a
 * failure here can never roll that back. Modulith writes the event to
 * {@code event_publication} in auth's own transaction, so a crash before this runs replays
 * it rather than leaving a signed-up user with no way to verify their address.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationEmailListener {

    private final NotificationSender notificationSender;

    @ApplicationModuleListener
    public void on(VerificationRequestedEvent event) {
        notificationSender.sendEmail(
                event.email(),
                "Confirm your email",
                VerificationEmailTemplate.render(event.name(), event.link()));
        log.info("Verification email sent: userId={}", event.userId());
    }
}
