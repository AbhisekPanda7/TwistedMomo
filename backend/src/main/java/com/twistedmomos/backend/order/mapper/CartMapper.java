package com.twistedmomos.backend.order.mapper;

import com.twistedmomos.backend.order.dto.response.CartItemResponse;
import com.twistedmomos.backend.order.dto.response.CartResponse;
import com.twistedmomos.backend.order.entity.Cart;
import com.twistedmomos.backend.order.entity.CartItem;
import com.twistedmomos.backend.restaurant.entity.MenuItem;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/** Cart totals are derived on read, so the cart always reflects current menu prices. */
@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        // Sorted by id so the cart does not reshuffle between requests; the collection
        // itself has no ordering guarantee.
        List<CartItemResponse> items = cart.getItems().stream()
                .sorted(Comparator.comparing(CartItem::getId))
                .map(this::toItemResponse)
                .toList();

        int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, totalItems, subtotal);
    }

    public CartItemResponse toItemResponse(CartItem item) {
        MenuItem menuItem = item.getMenuItem();
        return new CartItemResponse(
                item.getId(),
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getImageUrl(),
                menuItem.getPrice(),
                menuItem.isAvailable(),
                item.getQuantity(),
                menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
