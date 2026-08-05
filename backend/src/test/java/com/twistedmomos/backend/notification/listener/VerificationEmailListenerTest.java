package com.twistedmomos.backend.notification.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.twistedmomos.backend.auth.event.VerificationRequestedEvent;
import com.twistedmomos.backend.notification.api.NotificationSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class VerificationEmailListenerTest {

    @Mock private NotificationSender notificationSender;

    @InjectMocks private VerificationEmailListener listener;

    @Test
    void sendsTheRenderedLinkToTheAddressOnTheEvent() {
        var event = new VerificationRequestedEvent(
                1L, "verify@example.com", "Verify Tester", "https://twistedmomos.tech/verify-email?token=abc");

        listener.on(event);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(notificationSender).sendEmail(eq("verify@example.com"), anyString(), body.capture());
        assertThat(body.getValue()).contains(event.link());
    }

    /**
     * A swallowed send failure would leave the token committed with no email ever sent and
     * no retry. Letting it propagate leaves the publication incomplete so Modulith's outbox
     * retries — this is what makes the class Javadoc's replay claim true.
     */
    @Test
    void propagatesASendFailureSoTheOutboxRetries() {
        var event = new VerificationRequestedEvent(
                1L, "verify@example.com", "Verify Tester", "https://twistedmomos.tech/verify-email?token=abc");
        doThrow(new RestClientException("Resend is down"))
                .when(notificationSender).sendEmail(any(), any(), any());

        assertThatThrownBy(() -> listener.on(event)).isInstanceOf(RestClientException.class);
    }
}

