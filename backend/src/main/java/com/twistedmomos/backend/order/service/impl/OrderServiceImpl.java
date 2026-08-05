package com.twistedmomos.backend.order.service.impl;

import com.twistedmomos.backend.order.dto.request.PlaceOrderRequest;
import com.twistedmomos.backend.order.dto.response.OrderItemResponse;
import com.twistedmomos.backend.order.dto.response.OrderResponse;
import com.twistedmomos.backend.order.dto.response.OrderSummaryResponse;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import com.twistedmomos.backend.order.entity.Cart;
import com.twistedmomos.backend.order.entity.CartItem;
import com.twistedmomos.backend.restaurant.entity.MenuItem;
import com.twistedmomos.backend.order.entity.Order;
import com.twistedmomos.backend.order.entity.OrderItem;
import com.twistedmomos.backend.order.entity.OrderStatus;
import com.twistedmomos.backend.order.event.OrderPlacedEvent;
import com.twistedmomos.backend.order.mapper.OrderMapper;
import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.auth.address.AddressService;
import com.twistedmomos.backend.order.exception.EmailNotVerifiedException;
import com.twistedmomos.backend.order.exception.EmptyCartException;
import com.twistedmomos.backend.order.exception.InvalidOrderStatusTransitionException;
import com.twistedmomos.backend.order.exception.ItemUnavailableException;
import com.twistedmomos.backend.order.exception.MissingDeliveryAddressException;
import com.twistedmomos.backend.shared.exception.ResourceNotFoundException;
import com.twistedmomos.backend.order.repository.CartRepository;
import com.twistedmomos.backend.order.repository.OrderRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import com.twistedmomos.backend.order.service.OrderService;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final ApplicationEventPublisher events;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        log.info("Placing order: userId={}", userId);

        // Fetched rather than referenced: the response carries the customer's name and
        // email, so a lazy proxy here would only force a second query later.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));

        // Ordering is where a reachable address actually matters — we need to be able to
        // contact them about the order. Checked before the cart so the message names the
        // real problem instead of reporting an empty cart.
        if (!user.isEmailVerified()) {
            log.warn("Order rejected — email not verified: userId={}", userId);
            throw new EmailNotVerifiedException(
                    "Confirm your email before placing an order. Check your inbox for the link.");
        }

        Cart cart = cartRepository.findByUserIdWithItems(userId).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            log.warn("Order rejected — empty cart: userId={}", userId);
            throw new EmptyCartException("Your cart is empty");
        }
        log.info("Cart loaded: cartId={} distinctItems={}", cart.getId(), cart.getItems().size());

        // Availability can have changed since items were added to the cart — re-check at order time.
        for (CartItem cartItem : cart.getItems()) {
            if (!cartItem.getMenuItem().isAvailable()) {
                log.warn("Order rejected — item unavailable: userId={} menuItemId={}",
                        userId, cartItem.getMenuItem().getId());
                throw new ItemUnavailableException(
                        cartItem.getMenuItem().getName() + " is no longer available — please remove it from your cart");
            }
        }

        // Either a saved address or typed fields. Bean validation cannot express
        // "required unless addressId is set", so the choice is made here.
        String recipientName = request.recipientName();
        String phone = request.phone();
        String addressLine1 = request.addressLine1();
        String addressLine2 = request.addressLine2();
        String city = request.city();
        String postalCode = request.postalCode();

        if (request.addressId() != null) {
            var saved = addressService.findOwned(userId, request.addressId());
            recipientName = saved.getRecipientName();
            phone = saved.getPhone();
            addressLine1 = saved.getAddressLine1();
            addressLine2 = saved.getAddressLine2();
            city = saved.getCity();
            postalCode = saved.getPostalCode();
        } else if (isBlank(recipientName) || isBlank(phone) || isBlank(addressLine1)
                || isBlank(city) || isBlank(postalCode)) {
            log.warn("Order rejected — no delivery address: userId={}", userId);
            throw new MissingDeliveryAddressException(
                    "Choose a saved address or enter a delivery address");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .subtotal(BigDecimal.ZERO)
                .totalItems(0)
                .recipientName(recipientName)
                .phone(phone)
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .city(city)
                .postalCode(postalCode)
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
        log.info("Order totals computed: units={} subtotal={}", totalItems, subtotal);

        Order saved = orderRepository.save(order);
        log.info("Order placed: orderId={} userId={} status={} units={} subtotal={}",
                saved.getId(), userId, saved.getStatus(), totalItems, subtotal);

        // orphanRemoval on Cart.items deletes the cart_items rows on flush — the cart is now empty.
        cart.getItems().clear();
        log.info("Cart cleared after order: cartId={} orderId={}", cart.getId(), saved.getId());

        // Written to the event_publication table inside this transaction, so a crash
        // before the listener runs replays it rather than losing it. Subscribers act
        // after commit and cannot fail the order.
        events.publishEvent(toPlacedEvent(saved));

        // Everything the response needs is already loaded — re-querying the order
        // here cost an extra round trip per placement for nothing.
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PageResponse<OrderSummaryResponse> listMyOrders(Long userId, Pageable pageable) {
        return PageResponse.of(orderRepository.findByUserId(userId, pageable).map(orderMapper::toSummary));
    }

    @Override
    @Transactional
    public OrderResponse getMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> {
                    // The query is scoped by userId, so a miss is either a genuinely absent
                    // order or someone else's. Both answer 404 deliberately (see CLAUDE.md);
                    // only this log distinguishes them for an operator.
                    log.warn("Order not found or not owned: orderId={} userId={}", orderId, userId);
                    return new ResourceNotFoundException("Order " + orderId + " not found");
                });
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(Long userId, Long orderId) {
        log.info("Cancelling order: orderId={} userId={}", orderId, userId);

        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> {
                    log.warn("Cancellation rejected — order not found or not owned: orderId={} userId={}",
                            orderId, userId);
                    return new ResourceNotFoundException("Order " + orderId + " not found");
                });
        if (!order.getStatus().isCancellableByCustomer()) {
            log.warn("Cancellation rejected — status not cancellable: orderId={} status={}",
                    orderId, order.getStatus());
            throw new InvalidOrderStatusTransitionException(
                    "This order can no longer be cancelled (current status: " + order.getStatus() + ")");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledBy("CUSTOMER");
        log.info("Order cancelled: orderId={} userId={} from={} to={}",
                orderId, userId, OrderStatus.PENDING, OrderStatus.CANCELLED);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public PageResponse<OrderSummaryResponse> listAllOrders(String statusFilter, Pageable pageable) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return PageResponse.of(orderRepository.findAllWithUser(pageable).map(orderMapper::toSummary));
        }
        OrderStatus status = parseStatus(statusFilter);
        return PageResponse.of(orderRepository.findByStatusWithUser(status, pageable).map(orderMapper::toSummary));
    }

    @Override
    @Transactional
    public OrderResponse getOrder(Long orderId) {
        return orderMapper.toResponse(reloadWithDetails(orderId));
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, String status, String reason) {
        OrderStatus newStatus = parseStatus(status);
        Order order = reloadWithDetails(orderId);
        OrderStatus current = order.getStatus();
        log.info("Updating order status: orderId={} from={} to={}", orderId, current, newStatus);

        if (!current.canTransitionTo(newStatus)) {
            log.warn("Status transition rejected: orderId={} from={} to={} allowed={}",
                    orderId, current, newStatus, current.allowedNext());
            throw new InvalidOrderStatusTransitionException(
                    "Cannot move order from " + current + " to " + newStatus);
        }
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.CANCELLED) {
            order.setCancelledBy("RESTAURANT");
            order.setCancellationReason(reason);
        }
        log.info("Order status changed: orderId={} from={} to={}", orderId, current, newStatus);
        return orderMapper.toResponse(order);
    }

    private OrderPlacedEvent toPlacedEvent(Order order) {
        return new OrderPlacedEvent(
                order.getId(),
                order.getUser().getId(),
                order.getCreatedAt(),
                order.getSubtotal(),
                order.getItems().stream()
                        .map(item -> new OrderPlacedEvent.LineItem(
                                item.getMenuItem() == null ? null : item.getMenuItem().getId(),
                                item.getMenuItemName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getLineTotal()))
                        .toList(),
                new OrderPlacedEvent.DeliveryAddress(
                        order.getRecipientName(),
                        order.getPhone(),
                        order.getAddressLine1(),
                        order.getAddressLine2(),
                        order.getCity(),
                        order.getPostalCode()));
    }

    private Order reloadWithDetails(Long orderId) {
        return orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found: orderId={}", orderId);
                    return new ResourceNotFoundException("Order " + orderId + " not found");
                });
    }

    private OrderStatus parseStatus(String value) {
        try {
            return OrderStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown order status requested: value={}", value);
            throw new InvalidOrderStatusTransitionException("Unknown order status: " + value);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }



}
