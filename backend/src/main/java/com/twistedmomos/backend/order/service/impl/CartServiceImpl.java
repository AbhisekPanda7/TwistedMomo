package com.twistedmomos.backend.order.service.impl;

import com.twistedmomos.backend.order.dto.request.AddCartItemRequest;
import com.twistedmomos.backend.order.dto.request.UpdateCartItemRequest;
import com.twistedmomos.backend.order.dto.response.CartItemResponse;
import com.twistedmomos.backend.order.dto.response.CartResponse;
import com.twistedmomos.backend.order.entity.Cart;
import com.twistedmomos.backend.order.entity.CartItem;
import com.twistedmomos.backend.order.mapper.CartMapper;
import com.twistedmomos.backend.restaurant.entity.MenuItem;
import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.order.exception.ItemUnavailableException;
import com.twistedmomos.backend.shared.exception.ResourceNotFoundException;
import com.twistedmomos.backend.order.repository.CartItemRepository;
import com.twistedmomos.backend.order.repository.CartRepository;
import com.twistedmomos.backend.restaurant.repository.MenuItemRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import com.twistedmomos.backend.order.service.CartService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse getCart(Long userId) {
        return cartMapper.toResponse(getOrCreateCart(userId));
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        log.info("Adding cart item: userId={} menuItemId={} quantity={}",
                userId, request.menuItemId(), request.quantity());

        Cart cart = getOrCreateCart(userId);
        MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
                .orElseThrow(() -> {
                    log.warn("Cart add rejected — menu item not found: menuItemId={}", request.menuItemId());
                    return new ResourceNotFoundException("Menu item " + request.menuItemId() + " not found");
                });
        if (!menuItem.isAvailable()) {
            log.warn("Cart add rejected — item unavailable: menuItemId={}", menuItem.getId());
            throw new ItemUnavailableException(menuItem.getName() + " is currently unavailable");
        }

        CartItem item = cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItem.getId())
                .orElseGet(() -> {
                    CartItem created = CartItem.builder().cart(cart).menuItem(menuItem).quantity(0).build();
                    cart.getItems().add(created);
                    return created;
                });

        int requested = item.getQuantity() + request.quantity();
        int applied = Math.min(requested, MAX_QUANTITY_PER_ITEM);
        if (applied < requested) {
            // The clamp is silent to the caller — the response just shows the capped
            // quantity — so this is the only trace of why they did not get what they asked for.
            log.warn("Cart quantity capped: cartId={} menuItemId={} requested={} applied={}",
                    cart.getId(), menuItem.getId(), requested, applied);
        }
        item.setQuantity(applied);
        cartItemRepository.save(item);
        log.info("Cart item added: cartId={} menuItemId={} quantity={}",
                cart.getId(), menuItem.getId(), applied);

        return cartMapper.toResponse(reload(userId));
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item: userId={} cartItemId={} quantity={}",
                userId, cartItemId, request.quantity());

        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> {
                    // Scoped by cart id, so a miss also covers someone else's cart item.
                    log.warn("Cart update rejected — item not in this cart: cartItemId={} cartId={}",
                            cartItemId, cart.getId());
                    return new ResourceNotFoundException("Cart item " + cartItemId + " not found");
                });
        int previous = item.getQuantity();
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        log.info("Cart item updated: cartId={} cartItemId={} from={} to={}",
                cart.getId(), cartItemId, previous, request.quantity());

        return cartMapper.toResponse(reload(userId));
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        log.info("Removing cart item: userId={} cartItemId={}", userId, cartItemId);

        Cart cart = getOrCreateCart(userId);
        // Removing via the repository directly conflicts with Cart.items' cascade=ALL: Hibernate
        // reconciles the still-attached collection at flush time and the row survives. Removing
        // from the collection itself is what orphanRemoval is for — that's what actually deletes it.
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        if (!removed) {
            log.warn("Cart remove rejected — item not in this cart: cartItemId={} cartId={}",
                    cartItemId, cart.getId());
            throw new ResourceNotFoundException("Cart item " + cartItemId + " not found");
        }
        log.info("Cart item removed: cartId={} cartItemId={} remaining={}",
                cart.getId(), cartItemId, cart.getItems().size());
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        int removed = cart.getItems().size();
        // orphanRemoval on Cart.items deletes the rows on flush — no explicit repository call needed.
        cart.getItems().clear();
        log.info("Cart cleared: cartId={} itemsRemoved={}", cart.getId(), removed);
        return cartMapper.toResponse(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    User userRef = userRepository.getReferenceById(userId);
                    Cart created = cartRepository.save(Cart.builder().user(userRef).build());
                    log.info("Cart created: cartId={} userId={}", created.getId(), userId);
                    return created;
                });
    }

    private Cart reload(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> {
                    log.error("Cart disappeared mid-request: userId={}", userId);
                    return new IllegalStateException("Cart for user " + userId + " disappeared mid-request");
                });
    }


}
