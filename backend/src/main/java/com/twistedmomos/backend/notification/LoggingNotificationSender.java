package com.twistedmomos.backend.notification;

import com.twistedmomos.backend.notification.api.NotificationSender;
import lombok.extern.slf4j.Slf4j;

/**
 * Fallback when no provider key is configured, so local development needs no Resend
 * account — see NotificationConfig.
 *
 * <p>The body goes to debug: verification links are single-use and short-lived, but they
 * are still credentials and do not belong in info-level logs.
 */
@Slf4j
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public void sendEmail(String toAddress, String subject, String htmlBody) {
        log.info("Email not sent — no provider configured: subject={}", subject);
        log.debug("Email body that would have been sent: {}", htmlBody);
    }
}
