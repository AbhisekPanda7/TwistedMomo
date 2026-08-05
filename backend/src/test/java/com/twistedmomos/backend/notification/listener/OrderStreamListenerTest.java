package com.twistedmomos.backend.notification.listener;

import static org.mockito.Mockito.verify;

import com.twistedmomos.backend.notification.stream.OrderEventBroadcaster;
import com.twistedmomos.backend.order.entity.OrderStatus;
import com.twistedmomos.backend.order.event.OrderPlacedEvent;
import com.twistedmomos.backend.order.event.OrderStatusChangedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderStreamListenerTest {

    private static final Instant AT = Instant.parse("2026-08-05T12:00:00Z");

    @Mock private OrderEventBroadcaster broadcaster;
    @InjectMocks private OrderStreamListener listener;

    private static OrderPlacedEvent placedEvent() {
        return new OrderPlacedEvent(
                42L,
                7L,
                AT,
                new BigDecimal("260.00"),
                List.of(),
                new OrderPlacedEvent.DeliveryAddress("N", "9", "L1", null, "C", "751024"));
    }

    @Test
    void aNewOrderReachesTheQueue() {
        listener.onPlaced(placedEvent());

        verify(broadcaster).publish(42L);
    }

    @Test
    void aStatusChangeReachesTheQueue() {
        listener.onStatusChanged(new OrderStatusChangedEvent(
                42L, 7L, OrderStatus.PENDING, OrderStatus.CONFIRMED, AT));

        verify(broadcaster).publish(42L);
    }
}
