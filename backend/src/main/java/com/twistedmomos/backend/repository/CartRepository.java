package com.twistedmomos.backend.repository;

import com.twistedmomos.backend.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    /** Fetch-joins items -> menuItem -> category in one round trip so building CartResponse never triggers N+1 lazy loads. */
    @Query("select distinct c from Cart c "
            + "left join fetch c.items ci "
            + "left join fetch ci.menuItem mi "
            + "left join fetch mi.category "
            + "where c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
}
