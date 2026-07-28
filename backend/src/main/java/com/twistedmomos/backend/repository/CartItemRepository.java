package com.twistedmomos.backend.repository;

import com.twistedmomos.backend.entity.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndMenuItemId(Long cartId, Long menuItemId);

    /** Scoped by cartId so a user can never touch another user's cart item by guessing an id. */
    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);
}
