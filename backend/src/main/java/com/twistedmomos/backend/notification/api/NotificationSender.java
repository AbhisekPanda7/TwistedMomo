package com.twistedmomos.backend.notification.api;

/** Outbound delivery. Callers name a recipient and a message, never a provider. */
public interface NotificationSender {

    void sendEmail(String toAddress, String subject, String htmlBody);
}
