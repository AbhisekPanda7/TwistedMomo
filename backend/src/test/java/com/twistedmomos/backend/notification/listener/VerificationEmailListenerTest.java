package com.twistedmomos.backend.notification.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.twistedmomos.backend.auth.event.VerificationRequestedEvent;
import com.twistedmomos.backend.notification.api.NotificationSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}

