package com.twistedmomos.backend.order.listener;

import com.twistedmomos.backend.auth.service.AddressService;
import com.twistedmomos.backend.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Remembers where an order went, so checkout can offer it back.
 *
 * <p>Lives in order rather than auth: auth cannot import an order event without creating a
 * module cycle. Async and after commit, so a failure here costs the customer a retype on
 * their next order, never the sale — the delivery address is already on the order row.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAddressListener {

    private final AddressService addressService;

    @ApplicationModuleListener
    public void on(OrderPlacedEvent event) {
        var address = event.deliveryAddress();
        addressService.remember(
                event.userId(),
                address.recipientName(),
                address.phone(),
                address.addressLine1(),
                address.addressLine2(),
                address.city(),
                address.postalCode());
        log.debug("Delivery address remembered: userId={} orderId={}", event.userId(), event.orderId());
    }
}
