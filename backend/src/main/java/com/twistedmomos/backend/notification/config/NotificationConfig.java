package com.twistedmomos.backend.notification.config;

import com.twistedmomos.backend.notification.LoggingNotificationSender;
import com.twistedmomos.backend.notification.ResendNotificationSender;
import com.twistedmomos.backend.notification.api.NotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    NotificationSender notificationSender(NotificationProperties properties) {
        return StringUtils.hasText(properties.apiKey())
                ? new ResendNotificationSender(RestClient.builder(), properties)
                : new LoggingNotificationSender();
    }
}
