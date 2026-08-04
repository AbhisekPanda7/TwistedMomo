package com.twistedmomos.backend.order.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
import com.twistedmomos.backend.order.exception.EmptyCartException;
import com.twistedmomos.backend.order.exception.InvalidOrderStatusTransitionException;
import com.twistedmomos.backend.order.exception.ItemUnavailableException;
import com.twistedmomos.backend.order.repository.CartRepository;
import com.twistedmomos.backend.order.repository.OrderRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private MenuItem momo;
    private MenuItem chowmein;

    private static final PlaceOrderRequest DELIVERY = new PlaceOrderRequest(
            "Order Tester", "9999999999", "221B Baker St", null, "Metropolis", "500001", null);

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name(RoleName.CUSTOMER).build();
        user = User.builder().id(1L).name("Order Tester").email("order.tester@example.com").role(role).build();

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

    @Test
    void placeOrder_withNoCartRow_rejectsAsEmpty() {
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(1L, DELIVERY))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void placeOrder_withAnEmptyCart_rejects() {
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cartWith()));

        assertThatThrownBy(() -> orderService.placeOrder(1L, DELIVERY))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void placeOrder_whenACartItemHasGoneUnavailable_rejects() {
        chowmein.setAvailable(false);
        Cart cart = cartWith(CartItem.builder().id(1L).menuItem(chowmein).quantity(1).build());
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.placeOrder(1L, DELIVERY))
                .isInstanceOf(ItemUnavailableException.class);
    }

    @Test
    void placeOrder_snapshotsLinesAndEmptiesTheCart() {
        Cart cart = cartWith(
                CartItem.builder().id(1L).menuItem(momo).quantity(2).build(),
                CartItem.builder().id(2L).menuItem(chowmein).quantity(1).build());
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(userRepository.getReferenceById(1L)).thenReturn(user);

        // save() assigns an id (simulating IDENTITY generation); reloadWithDetails() then
        // re-fetches by that id, so both stubs must resolve to the same saved instance.
        Order[] savedHolder = new Order[1];
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            order.setId(100L);
            savedHolder[0] = order;
            return order;
        });
        when(orderRepository.findByIdWithDetails(100L)).thenAnswer(inv -> Optional.of(savedHolder[0]));

        OrderResponse response = orderService.placeOrder(1L, DELIVERY);

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.items()).hasSize(2);
        assertThat(response.totalItems()).isEqualTo(3);
        assertThat(response.subtotal()).isEqualByComparingTo("260.00");
        assertThat(response.recipientName()).isEqualTo("Order Tester");
        assertThat(cart.getItems()).isEmpty();
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

        OrderResponse response = orderService.updateStatus(1L, "CONFIRMED");

        assertThat(response.status()).isEqualTo("CONFIRMED");
    }

    @Test
    void updateStatus_rejectsSkippingAStage() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        lenient().when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, "DELIVERED"))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void updateStatus_rejectsMovingOutOfATerminalState() {
        Order order = orderWithStatus(OrderStatus.DELIVERED);
        lenient().when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, "CANCELLED"))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void updateStatus_rejectsAnUnknownStatusValue() {
        assertThatThrownBy(() -> orderService.updateStatus(1L, "NONSENSE"))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    void cancelMyOrder_whilePending_succeeds() {
        Order order = orderWithStatus(OrderStatus.PENDING);
        when(orderRepository.findByIdAndUserIdWithDetails(1L, 1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancelMyOrder(1L, 1L);

        assertThat(response.status()).isEqualTo("CANCELLED");
    }

    @Test
    void cancelMyOrder_onceConfirmed_isNoLongerAllowed() {
        Order order = orderWithStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdAndUserIdWithDetails(1L, 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelMyOrder(1L, 1L))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }
}
