package com.twistedmomos.backend.notification.listener;

import com.twistedmomos.backend.notification.stream.OrderEventBroadcaster;
import com.twistedmomos.backend.order.event.OrderPlacedEvent;
import com.twistedmomos.backend.order.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Nudges connected operators to refetch the queue.
 *
 * <p>Deliberately NOT {@code @ApplicationModuleListener}: that registers the event in the
 * outbox and retries until a listener succeeds. A push to zero connected operators has
 * nothing to retry and no meaningful success, and a replay after restart would announce a
 * stale event to whoever connected since. Clients refetch on reconnect, so a missed push
 * costs nothing.
 */
@Component
@RequiredArgsConstructor
public class OrderStreamListener {

    private final OrderEventBroadcaster broadcaster;

    @TransactionalEventListener
    public void onPlaced(OrderPlacedEvent event) {
        broadcaster.publish(event.orderId());
    }

    @TransactionalEventListener
    public void onStatusChanged(OrderStatusChangedEvent event) {
        broadcaster.publish(event.orderId());
    }
}
