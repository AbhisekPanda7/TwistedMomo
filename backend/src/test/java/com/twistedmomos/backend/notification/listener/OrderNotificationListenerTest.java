package com.twistedmomos.backend.notification.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.notification.entity.Notification;
import com.twistedmomos.backend.notification.entity.NotificationType;
import com.twistedmomos.backend.notification.repository.NotificationRepository;
import com.twistedmomos.backend.order.entity.OrderStatus;
import com.twistedmomos.backend.order.event.OrderStatusChangedEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderNotificationListenerTest {

    private static final Instant AT = Instant.parse("2026-08-05T12:00:00Z");

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks private OrderNotificationListener listener;

    private static OrderStatusChangedEvent event(OrderStatus from, OrderStatus to) {
        return new OrderStatusChangedEvent(42L, 7L, from, to, AT);
    }

    @Test
    void writesOneRowAddressedToTheCustomer() {
        when(notificationRepository.existsByOrderIdAndTitle(eq(42L), any())).thenReturn(false);

        listener.on(event(OrderStatus.PENDING, OrderStatus.CONFIRMED));

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(7L);
        assertThat(saved.getValue().getOrderId()).isEqualTo(42L);
        assertThat(saved.getValue().getType()).isEqualTo(NotificationType.ORDER_STATUS);
        assertThat(saved.getValue().getTitle()).isNotBlank();
        assertThat(saved.getValue().getBody()).isNotBlank();
        assertThat(saved.getValue().getReadAt()).isNull();
    }

    /**
     * The outbox retries an incomplete publication, so the same event can arrive twice.
     * Without this guard the customer sees "Order confirmed" twice.
     */
    @Test
    void ignoresAnEventItHasAlreadyRecorded() {
        when(notificationRepository.existsByOrderIdAndTitle(eq(42L), any())).thenReturn(true);

        listener.on(event(OrderStatus.PENDING, OrderStatus.CONFIRMED));

        verify(notificationRepository, never()).save(any());
    }

    /** READY means "leaving the kitchen" — an internal step the customer gains nothing from. */
    @Test
    void staysQuietForInternalTransitions() {
        listener.on(event(OrderStatus.PREPARING, OrderStatus.READY));

        verify(notificationRepository, never()).save(any());
    }

    /** PENDING is the initial state, not a transition anyone announces. */
    @Test
    void staysQuietWhenAnOrderIsFirstCreated() {
        listener.on(event(null, OrderStatus.PENDING));

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void announcesEveryCustomerFacingTransition() {
        when(notificationRepository.existsByOrderIdAndTitle(any(), any())).thenReturn(false);

        listener.on(event(OrderStatus.PENDING, OrderStatus.CONFIRMED));
        listener.on(event(OrderStatus.CONFIRMED, OrderStatus.PREPARING));
        listener.on(event(OrderStatus.READY, OrderStatus.OUT_FOR_DELIVERY));
        listener.on(event(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED));
        listener.on(event(OrderStatus.PENDING, OrderStatus.CANCELLED));

        verify(notificationRepository, times(5)).save(any());
    }
}
