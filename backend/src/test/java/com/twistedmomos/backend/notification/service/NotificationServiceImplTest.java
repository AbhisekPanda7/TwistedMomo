package com.twistedmomos.backend.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.notification.entity.Notification;
import com.twistedmomos.backend.notification.entity.NotificationType;
import com.twistedmomos.backend.notification.mapper.NotificationMapper;
import com.twistedmomos.backend.notification.repository.NotificationRepository;
import com.twistedmomos.backend.notification.service.impl.NotificationServiceImpl;
import com.twistedmomos.backend.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final Long USER_ID = 7L;

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationMapper notificationMapper;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationRepository, notificationMapper);
    }

    @Test
    void markingReadStampsTheRow() {
        Notification n = Notification.builder()
                .userId(USER_ID).type(NotificationType.ORDER_STATUS)
                .title("t").body("b").orderId(1L).build();
        when(notificationRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(n));

        service.markRead(USER_ID, 5L);

        assertThat(n.getReadAt()).isNotNull();
    }

    /** Already read: the timestamp must not move, or "when did I see this" becomes a lie. */
    @Test
    void markingAnAlreadyReadNotificationDoesNotMoveTheTimestamp() {
        Notification n = Notification.builder()
                .userId(USER_ID).type(NotificationType.ORDER_STATUS)
                .title("t").body("b").orderId(1L).build();
        n.setReadAt(Instant.parse("2026-08-01T00:00:00Z"));
        when(notificationRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(n));

        service.markRead(USER_ID, 5L);

        assertThat(n.getReadAt()).isEqualTo(Instant.parse("2026-08-01T00:00:00Z"));
    }

    /** Someone else's notification is 404, not 403 — existence is not leaked. */
    @Test
    void anotherUsersNotificationIsNotFound() {
        when(notificationRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(USER_ID, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void unreadCountIsScopedToTheCaller() {
        when(notificationRepository.countByUserIdAndReadAtIsNull(USER_ID)).thenReturn(3L);

        assertThat(service.unreadCount(USER_ID)).isEqualTo(3L);
    }
}
