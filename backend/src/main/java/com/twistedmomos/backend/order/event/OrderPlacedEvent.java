package com.twistedmomos.backend.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Published after an order commits.
 *
 * <p>Carries values rather than entities: listeners run on another thread after the
 * transaction closes, and with {@code open-in-view=false} a lazy association would blow up
 * there. The line items are snapshots for the same reason orders store them — a later menu
 * edit must not rewrite what was ordered.
 */
public record OrderPlacedEvent(
        Long orderId,
        Long userId,
        Instant placedAt,
        BigDecimal subtotal,
        List<LineItem> items) {

    public record LineItem(Long menuItemId, String itemName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
}
