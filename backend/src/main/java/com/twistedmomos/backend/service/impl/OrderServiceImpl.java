package com.twistedmomos.backend.service.impl;

import com.twistedmomos.backend.dto.request.PlaceOrderRequest;
import com.twistedmomos.backend.dto.response.OrderItemResponse;
import com.twistedmomos.backend.dto.response.OrderResponse;
import com.twistedmomos.backend.dto.response.OrderSummaryResponse;
import com.twistedmomos.backend.dto.response.PageResponse;
import com.twistedmomos.backend.entity.Cart;
import com.twistedmomos.backend.entity.CartItem;
import com.twistedmomos.backend.entity.MenuItem;
import com.twistedmomos.backend.entity.Order;
import com.twistedmomos.backend.entity.OrderItem;
import com.twistedmomos.backend.entity.OrderStatus;
import com.twistedmomos.backend.entity.User;
import com.twistedmomos.backend.exception.EmptyCartException;
import com.twistedmomos.backend.exception.InvalidOrderStatusTransitionException;
import com.twistedmomos.backend.exception.ItemUnavailableException;
import com.twistedmomos.backend.exception.ResourceNotFoundException;
import com.twistedmomos.backend.repository.CartRepository;
import com.twistedmomos.backend.repository.OrderRepository;
import com.twistedmomos.backend.repository.UserRepository;
import com.twistedmomos.backend.service.OrderService;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, EnumSet.of(OrderStatus.READY, OrderStatus.CANCELLED),
            OrderStatus.READY, EnumSet.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED),
            OrderStatus.OUT_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class)
    );

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Cart cart = cartRepository.findByUserIdWithItems(userId).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Your cart is empty");
        }

        // Availability can have changed since items were added to the cart — re-check at order time.
        for (CartItem cartItem : cart.getItems()) {
            if (!cartItem.getMenuItem().isAvailable()) {
                throw new ItemUnavailableException(
                        cartItem.getMenuItem().getName() + " is no longer available — please remove it from your cart");
            }
        }

        User userRef = userRepository.getReferenceById(userId);
        Order order = Order.builder()
                .user(userRef)
                .status(OrderStatus.PENDING)
                .subtotal(BigDecimal.ZERO)
                .totalItems(0)
                .recipientName(request.recipientName())
                .phone(request.phone())
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .city(request.city())
                .postalCode(request.postalCode())
                .notes(request.notes())
                .build();

        int totalItems = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            MenuItem menuItem = cartItem.getMenuItem();
            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .menuItemName(menuItem.getName())
                    .unitPrice(menuItem.getPrice())
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .build();
            order.getItems().add(orderItem);
            totalItems += cartItem.getQuantity();
            subtotal = subtotal.add(lineTotal);
        }
        order.setTotalItems(totalItems);
        order.setSubtotal(subtotal);

        Order saved = orderRepository.save(order);

        // orphanRemoval on Cart.items deletes the cart_items rows on flush — the cart is now empty.
        cart.getItems().clear();

        return toResponse(reloadWithDetails(saved.getId()));
    }

    @Override
    @Transactional
    public PageResponse<OrderSummaryResponse> listMyOrders(Long userId, Pageable pageable) {
        return PageResponse.of(orderRepository.findByUserId(userId, pageable).map(this::toSummary));
    }

    @Override
    @Transactional
    public OrderResponse getMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " not found"));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusTransitionException(
                    "This order can no longer be cancelled (current status: " + order.getStatus() + ")");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(order);
    }

    @Override
    @Transactional
    public PageResponse<OrderSummaryResponse> listAllOrders(String statusFilter, Pageable pageable) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return PageResponse.of(orderRepository.findAllWithUser(pageable).map(this::toSummary));
        }
        OrderStatus status = parseStatus(statusFilter);
        return PageResponse.of(orderRepository.findByStatusWithUser(status, pageable).map(this::toSummary));
    }

    @Override
    @Transactional
    public OrderResponse getOrder(Long orderId) {
        return toResponse(reloadWithDetails(orderId));
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, String status) {
        OrderStatus newStatus = parseStatus(status);
        Order order = reloadWithDetails(orderId);
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidOrderStatusTransitionException(
                    "Cannot move order from " + order.getStatus() + " to " + newStatus);
        }
        order.setStatus(newStatus);
        return toResponse(order);
    }

    private Order reloadWithDetails(Long orderId) {
        return orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " not found"));
    }

    private OrderStatus parseStatus(String value) {
        try {
            return OrderStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusTransitionException("Unknown order status: " + value);
        }
    }

    private OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalItems(),
                order.getSubtotal(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getCreatedAt()
        );
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getItems().stream().map(this::toItemResponse).toList(),
                order.getTotalItems(),
                order.getSubtotal(),
                order.getRecipientName(),
                order.getPhone(),
                order.getAddressLine1(),
                order.getAddressLine2(),
                order.getCity(),
                order.getPostalCode(),
                order.getNotes(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getMenuItem() != null ? item.getMenuItem().getId() : null,
                item.getMenuItemName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
