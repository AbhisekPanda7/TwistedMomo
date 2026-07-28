package com.twistedmomos.backend.service.impl;

import com.twistedmomos.backend.dto.request.AddCartItemRequest;
import com.twistedmomos.backend.dto.request.UpdateCartItemRequest;
import com.twistedmomos.backend.dto.response.CartItemResponse;
import com.twistedmomos.backend.dto.response.CartResponse;
import com.twistedmomos.backend.entity.Cart;
import com.twistedmomos.backend.entity.CartItem;
import com.twistedmomos.backend.entity.MenuItem;
import com.twistedmomos.backend.entity.User;
import com.twistedmomos.backend.exception.ItemUnavailableException;
import com.twistedmomos.backend.exception.ResourceNotFoundException;
import com.twistedmomos.backend.repository.CartItemRepository;
import com.twistedmomos.backend.repository.CartRepository;
import com.twistedmomos.backend.repository.MenuItemRepository;
import com.twistedmomos.backend.repository.UserRepository;
import com.twistedmomos.backend.service.CartService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    /** Sanity cap on a single line's quantity, independent of the per-request 1-20 validation, since repeated adds merge. */
    private static final int MAX_QUANTITY_PER_ITEM = 20;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse getCart(Long userId) {
        return toResponse(getOrCreateCart(userId));
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item " + request.menuItemId() + " not found"));
        if (!menuItem.isAvailable()) {
            throw new ItemUnavailableException(menuItem.getName() + " is currently unavailable");
        }

        CartItem item = cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItem.getId())
                .orElseGet(() -> {
                    CartItem created = CartItem.builder().cart(cart).menuItem(menuItem).quantity(0).build();
                    cart.getItems().add(created);
                    return created;
                });
        item.setQuantity(Math.min(item.getQuantity() + request.quantity(), MAX_QUANTITY_PER_ITEM));
        cartItemRepository.save(item);

        return toResponse(reload(userId));
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item " + cartItemId + " not found"));
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return toResponse(reload(userId));
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        // Removing via the repository directly conflicts with Cart.items' cascade=ALL: Hibernate
        // reconciles the still-attached collection at flush time and the row survives. Removing
        // from the collection itself is what orphanRemoval is for — that's what actually deletes it.
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item " + cartItemId + " not found");
        }
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        // orphanRemoval on Cart.items deletes the rows on flush — no explicit repository call needed.
        cart.getItems().clear();
        return toResponse(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    User userRef = userRepository.getReferenceById(userId);
                    return cartRepository.save(Cart.builder().user(userRef).build());
                });
    }

    private Cart reload(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new IllegalStateException("Cart for user " + userId + " disappeared mid-request"));
    }

    private CartResponse toResponse(Cart cart) {
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

    private CartItemResponse toItemResponse(CartItem item) {
        MenuItem menuItem = item.getMenuItem();
        BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getImageUrl(),
                menuItem.getPrice(),
                menuItem.isAvailable(),
                item.getQuantity(),
                lineTotal
        );
    }
}
