package com.twistedmomos.backend.order.event;

import com.twistedmomos.backend.order.entity.OrderStatus;
import java.time.Instant;

/**
 * Published after a status transition commits. Carries values rather than entities for the
 * same reason as {@link OrderPlacedEvent}: listeners run on another thread after the
 * transaction closes, and with {@code open-in-view=false} a lazy association would blow up
 * there. Carries both ends of the move so a listener can describe it without reading the
 * order back.
 */
public record OrderStatusChangedEvent(
        Long orderId, Long userId, OrderStatus from, OrderStatus to, Instant changedAt) {}
