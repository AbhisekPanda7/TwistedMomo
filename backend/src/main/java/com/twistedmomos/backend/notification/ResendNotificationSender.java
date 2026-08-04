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
 * <p>Send failures are logged, not thrown: a provider outage must not fail the
 * registration that triggered the email. The user can request a new link.
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
            // No recipient address in the log — see LoggingPiiTest.
            log.error("Email send failed: subject={}", subject, ex);
        }
    }
}
