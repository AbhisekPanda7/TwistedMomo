package com.twistedmomos.backend.reporting;

import com.twistedmomos.backend.order.event.OrderPlacedEvent;
import com.twistedmomos.backend.reporting.entity.OrderReportLine;
import com.twistedmomos.backend.reporting.repository.OrderReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Turns a placed order into report rows.
 *
 * <p>{@code @ApplicationModuleListener} is async, {@code REQUIRES_NEW} and
 * {@code AFTER_COMMIT} together: the order commits alone, and anything that goes wrong
 * here cannot roll it back. Modulith writes the event to {@code event_publication} in the
 * order's own transaction, so a crash before this runs replays it rather than losing the
 * row — which is why reporting can be rebuilt but never silently misses an order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReportListener {

    private final OrderReportRepository reportRepository;

    @ApplicationModuleListener
    public void on(OrderPlacedEvent event) {
        // A retried publication would otherwise duplicate every line. The unique
        // constraint on (order_id, item_name) is the backstop; this is the cheap check.
        if (reportRepository.existsByOrderId(event.orderId())) {
            log.debug("Report lines already recorded: orderId={}", event.orderId());
            return;
        }

        var lines = event.items().stream()
                .map(item -> OrderReportLine.builder()
                        .orderId(event.orderId())
                        .userId(event.userId())
                        .menuItemId(item.menuItemId())
                        .itemName(item.itemName())
                        .quantity(item.quantity())
                        .unitPrice(item.unitPrice())
                        .lineTotal(item.lineTotal())
                        .placedAt(event.placedAt())
                        .build())
                .toList();

        reportRepository.saveAll(lines);
        log.info("Report lines recorded: orderId={} lines={}", event.orderId(), lines.size());
    }
}
