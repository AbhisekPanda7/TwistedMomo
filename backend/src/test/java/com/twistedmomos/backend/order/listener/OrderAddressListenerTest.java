package com.twistedmomos.backend.order.listener;

import static org.mockito.Mockito.verify;

import com.twistedmomos.backend.auth.address.AddressService;
import com.twistedmomos.backend.order.event.OrderPlacedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderAddressListenerTest {

    @Mock private AddressService addressService;

    @InjectMocks private OrderAddressListener listener;

    @Test
    void remembersTheAddressTheOrderWasDeliveredTo() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                100L, 7L, Instant.parse("2026-08-04T12:00:00Z"), new BigDecimal("80.00"),
                List.of(new OrderPlacedEvent.LineItem(
                        1L, "Veg Momo", 1, new BigDecimal("80.00"), new BigDecimal("80.00"))),
                new OrderPlacedEvent.DeliveryAddress(
                        "Tester", "9999999999", "1 Test St", "Near the cart", "Cuttack", "753014"));

        listener.on(event);

        verify(addressService).remember(
                7L, "Tester", "9999999999", "1 Test St", "Near the cart", "Cuttack", "753014");
    }
}
