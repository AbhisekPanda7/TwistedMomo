package com.twistedmomos.backend.order.service.impl;

import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.order.dto.request.PlaceOrderRequest;
import com.twistedmomos.backend.order.dto.response.OrderResponse;
import com.twistedmomos.backend.order.entity.Cart;
import com.twistedmomos.backend.order.entity.CartItem;
import com.twistedmomos.backend.restaurant.entity.Category;
import com.twistedmomos.backend.restaurant.entity.MenuItem;
import com.twistedmomos.backend.order.entity.Order;
import com.twistedmomos.backend.order.entity.OrderStatus;
import com.twistedmomos.backend.auth.entity.Role;
import com.twistedmomos.backend.auth.entity.RoleName;
import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.order.exception.EmailNotVerifiedException;
import com.twistedmomos.backend.order.exception.EmptyCartException;
import com.twistedmomos.backend.order.exception.InvalidOrderStatusTransitionException;
import com.twistedmomos.backend.order.exception.ItemUnavailableException;
import com.twistedmomos.backend.order.exception.MissingDeliveryAddressException;
import com.twistedmomos.backend.order.event.OrderStatusChangedEvent;
import com.twistedmomos.backend.auth.address.AddressService;
import com.twistedmomos.backend.order.repository.CartRepository;
import com.twistedmomos.backend.order.repository.OrderRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import com.twistedmomos.backend.order.mapper.OrderMapper;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher events;

    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private MenuItem momo;
    private MenuItem chowmein;

    private static final PlaceOrderRequest DELIVERY = new PlaceOrderRequest(
            "Order Tester", "9999999999", "221B Baker St", null, "Metropolis", "500001", null, null);

    private static final PlaceOrderRequest BY_SAVED_ADDRESS = new PlaceOrderRequest(
            null, null, null, null, null, null, "Leave at the gate", 42L);

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name(RoleName.CUSTOMER).build();
        user = User.builder().id(1L).name("Order Tester").email("order.tester@example.com")
                .roles(Set.of(role)).emailVerified(true).build();

        Category category = Category.builder().id(1L).name("Steam").slug("steam").displayOrder(0).active(true).build();
        momo = MenuItem.builder().id(1L).category(category).name("Veg Momo").slug("veg-momo")
                .price(new BigDecimal("80.00")).veg(true).available(true).build();
        chowmein = MenuItem.builder().id(2L).category(category).name("Chowmein").slug("chowmein")
                .price(new BigDecimal("100.00")).veg(true).available(true).build();
    }

    private Cart cartWith(CartItem... items) {
        Cart cart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
        for (CartItem item : items) {
            cart.getItems().add(item);
        }
        return cart;
    }

    /** Verification gates ordering, not browsing — we must be able to reach them about it. */
    @Test
    void placeOrder_withAnUnverifiedEmail_rejectsBeforeTouchingTheCart() {
        user.setEmailVerified(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> orderService.placeOrder(1L, DELIVERY))
                .isInstanceOf(EmailNotVerifiedException.class);
        verifyNoInteractions(cartRepository);
    }

    @Test
    void placeOrder_withNoCartRow_rejectsAsEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(1L, DELIVERY))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void placeOrder_withAnEmptyCart_rejects() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cartWith()));

        assertThatThrownBy(() -> orderService.placeOrder(1L, DELIVERY))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void placeOrder_whenACartItemHasGoneUnavailable_rejects() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        chowmein.setAvailable(false);
        Cart cart = cartWith(CartItem.builder().id(1L).menuItem(chowmein).quantity(1).build());
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.placeOrder(1L, DELIVERY))
                .isInstanceOf(ItemUnavailableException.class);
    }

    @Test
    void placeOrder_snapshotsLinesAndEmptiesTheCart() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Cart cart = cartWith(
                CartItem.builder().id(1L).menuItem(momo).quantity(2).build(),
                CartItem.builder().id(2L).menuItem(chowmein).quantity(1).build());
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));

        // save() assigns an id, standing in for IDENTITY generation. The response is
        // mapped from this instance directly — placeOrder no longer re-reads it.
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            order.setId(100L);
            return order;
        });

        OrderResponse response = orderService.placeOrder(1L, DELIVERY);

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.items()).hasSize(2);
        assertThat(response.totalItems()).isEqualTo(3);
        assertThat(response.subtotal()).isEqualByComparingTo("260.00");
        assertThat(response.recipientName()).isEqualTo("Order Tester");
        assertThat(cart.getItems()).isEmpty();
    }

    /** A saved address supplies the delivery details the request omits. */
    @Test
    void placeOrder_withASavedAddress_copiesItOntoTheOrder() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserIdWithItems(1L))
                .thenReturn(Optional.of(cartWith(CartItem.builder().id(1L).menuItem(momo).quantity(1).build())));
        when(addressService.findOwned(1L, 42L)).thenReturn(
                com.twistedmomos.backend.auth.entity.UserAddress.builder()
                        .id(42L).user(user).recipientName("Saved Name").phone("8888888888")
                        .addressLine1("9 Saved Rd").addressLine2(null)
                        .city("Cuttack").postalCode("753014").build());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            order.setId(100L);
            return order;
        });

        OrderResponse response = orderService.placeOrder(1L, BY_SAVED_ADDRESS);

        assertThat(response.recipientName()).isEqualTo("Saved Name");
        assertThat(response.addressLine1()).isEqualTo("9 Saved Rd");
        assertThat(response.notes()).isEqualTo("Leave at the gate");
    }

    /** Neither a saved address nor typed fields: nowhere to deliver. */
    @Test
    void placeOrder_withNoAddressAtAll_rejects() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserIdWithItems(1L))
                .thenReturn(Optional.of(cartWith(CartItem.builder().id(1L).menuItem(momo).quantity(1).build())));

        PlaceOrderRequest blank = new PlaceOrderRequest(null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> orderService.placeOrder(1L, blank))
                .isInstanceOf(MissingDeliveryAddressException.class);
    }

    private Order orderWithStatus(OrderStatus status) {
        return Order.builder()
                .id(1L).user(user).status(status)
                .subtotal(new BigDecimal("80.00")).totalItems(1)
                .recipientName("Order Tester").phone("9999999999")
                .addressLine1("221B Baker St").city("Metropolis").postalCode("500001")
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void updateStatus_allowsAValidForwardTransition() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        lenient().when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateStatus(1L, "CONFIRMED", null);

        assertThat(response.status()).isEqualTo("CONFIRMED");
    }

    @Test
    void updateStatus_rejectsSkippingAStage() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        lenient().when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, "DELIVERED", null))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void updateStatus_rejectsMovingOutOfATerminalState() {
        Order order = orderWithStatus(OrderStatus.DELIVERED);
        lenient().when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, "CANCELLED", null))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void updateStatus_rejectsAnUnknownStatusValue() {
        assertThatThrownBy(() -> orderService.updateStatus(1L, "NONSENSE", null))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void cancelMyOrder_recordsWhoCancelled() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        when(orderRepository.findByIdAndUserIdWithDetails(1L, 1L)).thenReturn(Optional.of(order));

        orderService.cancelMyOrder(1L, 1L);

        assertThat(order.getCancelledBy()).isEqualTo("CUSTOMER");
    }

    @Test
    void updateStatus_restaurantDeclineRecordsTheReason() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        lenient().when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateStatus(1L, "CANCELLED", "Out of stock");

        assertThat(order.getCancelledBy()).isEqualTo("RESTAURANT");
        assertThat(order.getCancellationReason()).isEqualTo("Out of stock");
        // The columns exist to be seen by the customer — prove the mapper actually surfaces them.
        assertThat(response.cancelledBy()).isEqualTo("RESTAURANT");
        assertThat(response.cancellationReason()).isEqualTo("Out of stock");
    }

    /** Attribution belongs only to a cancellation — a normal transition must not set it. */
    @Test
    void updateStatus_confirmingAnOrderSetsNoAttribution() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        lenient().when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        orderService.updateStatus(1L, "CONFIRMED", null);

        assertThat(order.getCancelledBy()).isNull();
        assertThat(order.getCancellationReason()).isNull();
    }

    @Test
    void cancelMyOrder_whilePending_succeeds() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        when(orderRepository.findByIdAndUserIdWithDetails(1L, 1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancelMyOrder(1L, 1L);

        assertThat(response.status()).isEqualTo("CANCELLED");
    }

    @Test
    void updateStatus_publishesAnEventCarryingBothEnds() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        lenient().when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        orderService.updateStatus(1L, "CONFIRMED", null);

        ArgumentCaptor<OrderStatusChangedEvent> event =
                ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().orderId()).isEqualTo(1L);
        assertThat(event.getValue().userId()).isEqualTo(1L);
        assertThat(event.getValue().from()).isEqualTo(OrderStatus.PENDING);
        assertThat(event.getValue().to()).isEqualTo(OrderStatus.CONFIRMED);
    }

    /** A rejected transition must publish nothing — a listener would announce a change that never happened. */
    @Test
    void updateStatus_rejectedTransitionPublishesNothing() {
        Order order = orderWithStatus(OrderStatus.DELIVERED);
        lenient().when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, "PENDING", null))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);

        verify(events, never()).publishEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void cancelMyOrder_onceConfirmed_isNoLongerAllowed() {
        Order order = orderWithStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdAndUserIdWithDetails(1L, 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelMyOrder(1L, 1L))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }
}
