package com.twistedmomos.backend.notification;

import com.twistedmomos.backend.notification.api.NotificationSender;
import com.twistedmomos.backend.notification.config.NotificationProperties;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Resend HTTP adapter, active once an API key is configured.
 *
 * <p>Send failures propagate: every caller today runs inside an {@code @ApplicationModuleListener},
 * so a thrown exception leaves the publication incomplete and Modulith retries it — a provider
 * outage costs a delay, not a silently lost email. If a future caller sends synchronously inside
 * a request, it must decide its own fallback rather than relying on this class to swallow.
 */
@Slf4j
public class ResendNotificationSender implements NotificationSender {

    private static final String SEND_URL = "https://api.resend.com/emails";

    private final RestClient client;
    private final NotificationProperties properties;

    public ResendNotificationSender(RestClient.Builder builder, NotificationProperties properties) {
        this.properties = properties;
        this.client = builder
                .baseUrl(SEND_URL)
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public void sendEmail(String toAddress, String subject, String htmlBody) {
        Map<String, Object> payload = Map.of(
                "from", "%s <%s>".formatted(properties.fromName(), properties.fromEmail()),
                "to", List.of(toAddress),
                "subject", subject,
                "html", htmlBody);
        try {
            client.post().contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().toBodilessEntity();
            log.info("Email sent: subject={}", subject);
        } catch (RestClientException ex) {
            // No recipient address or provider message in the log — see LoggingPiiTest,
            // and Resend's error body can echo the "to" address back.
            log.error("Email send failed: subject={}", subject);
            throw ex;
        }
    }
}
