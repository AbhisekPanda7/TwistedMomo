package com.twistedmomos.backend.order.mapper;

import com.twistedmomos.backend.order.dto.response.OrderItemResponse;
import com.twistedmomos.backend.order.dto.response.OrderResponse;
import com.twistedmomos.backend.order.dto.response.OrderSummaryResponse;
import com.twistedmomos.backend.order.entity.Order;
import com.twistedmomos.backend.order.entity.OrderItem;
import org.springframework.stereotype.Component;

/**
 * Hand-written rather than MapStruct, unlike restaurant/ and auth/: these responses are
 * flattened across the order, its user and its lines, which MapStruct expresses only
 * through a chain of @Named helpers that is longer than the code it replaces.
 */
@Component
public class OrderMapper {

    /** Reads order.user — callers must have fetched it (see OrderRepository join fetches). */
    public OrderResponse toResponse(Order order) {
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
                order.getUpdatedAt());
    }

    public OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalItems(),
                order.getSubtotal(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getCreatedAt());
    }

    /** menuItemId is null once an admin hard-deletes the item; the name and price survive here. */
    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getMenuItem() == null ? null : item.getMenuItem().getId(),
                item.getMenuItemName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal());
    }
}
