package com.twistedmomos.backend.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.notification.config.NotificationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

class ResendNotificationSenderTest {

    private static final NotificationProperties PROPERTIES =
            new NotificationProperties("key", "Twisted Momos", "hello@twistedmomos.tech");

    /**
     * Before the notification-listener refactor this send sat inside the caller's own
     * transaction, so swallowing protected an unrelated registration from a provider outage.
     * The send is now @ApplicationModuleListener, AFTER_COMMIT, in its own transaction — a
     * swallowed failure here would instead leave a token committed with no email ever sent
     * and no retry. It must propagate so Modulith's outbox retries it.
     */
    @Test
    void propagatesASendFailureInsteadOfSwallowingIt() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        RestClient client = mock(RestClient.class);
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl(any(String.class))).thenReturn(builder);
        when(builder.defaultHeader(any(), any())).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(uriSpec);
        when(uriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        when(bodySpec.body(any(Object.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new RestClientException("Resend is down"));

        var sender = new ResendNotificationSender(builder, PROPERTIES);

        assertThatThrownBy(() -> sender.sendEmail("verify@example.com", "Confirm your email", "<p>hi</p>"))
                .isInstanceOf(RestClientException.class);
    }
}
