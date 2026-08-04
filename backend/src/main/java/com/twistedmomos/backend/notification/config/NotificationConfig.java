package com.twistedmomos.backend.notification.config;

import com.twistedmomos.backend.notification.LoggingNotificationSender;
import com.twistedmomos.backend.notification.ResendNotificationSender;
import com.twistedmomos.backend.notification.api.NotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * One sender, chosen by whether a key is present. Doing this in code rather than with
 * {@code @ConditionalOnProperty} on each adapter because that annotation cannot express
 * "set and non-empty" — {@code havingValue=""} matches any value, which registered both
 * and failed startup on an ambiguous bean.
 */
@Configuration
public class NotificationConfig {

    @Bean
    NotificationSender notificationSender(NotificationProperties properties, Environment environment) {
        String apiKey = properties.apiKey();

        // An unset ${RESEND_API_KEY} does not fail binding on a @ConfigurationProperties
        // record — it arrives as the literal placeholder text. Left alone that reads as
        // "configured", so the Resend adapter is built with the placeholder as its bearer
        // token and every send 401s while registration still returns 201.
        if (apiKey != null && apiKey.startsWith("${")) {
            if (environment.matchesProfiles("prod")) {
                throw new IllegalStateException(
                        "RESEND_API_KEY is not set. Production must not start without it — "
                                + "signups would succeed while no verification email is ever sent.");
            }
            apiKey = null;
        }

        return StringUtils.hasText(apiKey)
                ? new ResendNotificationSender(RestClient.builder(), properties)
                : new LoggingNotificationSender();
    }
}
