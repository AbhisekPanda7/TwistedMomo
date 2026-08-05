package com.twistedmomos.backend.notification.listener;

import com.twistedmomos.backend.notification.entity.Notification;
import com.twistedmomos.backend.notification.entity.NotificationType;
import com.twistedmomos.backend.notification.repository.NotificationRepository;
import com.twistedmomos.backend.order.entity.OrderStatus;
import com.twistedmomos.backend.order.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Turns a status change into a message for the customer.
 *
 * <p>{@code @ApplicationModuleListener} is async, {@code REQUIRES_NEW} and
 * {@code AFTER_COMMIT} together: the status change commits alone, and anything that goes
 * wrong here cannot roll it back. Modulith writes the event to {@code event_publication} in
 * the order's own transaction, so a crash before this runs replays it rather than dropping
 * the customer's notification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final NotificationRepository notificationRepository;

    @ApplicationModuleListener
    public void on(OrderStatusChangedEvent event) {
        String title = titleFor(event.to());
        if (title == null) {
            log.debug("No customer-facing message for transition: orderId={} to={}",
                    event.orderId(), event.to());
            return;
        }

        // A retried publication would otherwise duplicate the message. The unique
        // constraint on (order_id, title) is the backstop; this is the cheap check.
        if (notificationRepository.existsByOrderIdAndTitle(event.orderId(), title)) {
            log.debug("Notification already recorded: orderId={} title={}", event.orderId(), title);
            return;
        }

        notificationRepository.save(Notification.builder()
                .userId(event.userId())
                .type(NotificationType.ORDER_STATUS)
                .title(title)
                .body(bodyFor(event.to(), event.orderId()))
                .orderId(event.orderId())
                .build());
        log.info("Notification recorded: orderId={} userId={} to={}",
                event.orderId(), event.userId(), event.to());
    }

    /** Null means the customer gains nothing from hearing about this move. */
    private static String titleFor(OrderStatus to) {
        return switch (to) {
            case CONFIRMED -> "Order confirmed";
            case PREPARING -> "We're making your momos";
            case OUT_FOR_DELIVERY -> "Out for delivery";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Order cancelled";
            case PENDING, READY -> null;
        };
    }

    private static String bodyFor(OrderStatus to, Long orderId) {
        return switch (to) {
            case CONFIRMED -> "Order #" + orderId + " is confirmed and heading to the kitchen.";
            case PREPARING -> "Order #" + orderId + " is being prepared right now.";
            case OUT_FOR_DELIVERY -> "Order #" + orderId + " is on its way to you.";
            case DELIVERED -> "Order #" + orderId + " has been delivered. Enjoy.";
            case CANCELLED -> "Order #" + orderId + " was cancelled.";
            case PENDING, READY -> "";
        };
    }
}
