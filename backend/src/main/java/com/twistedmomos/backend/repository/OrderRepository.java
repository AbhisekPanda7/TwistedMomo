package com.twistedmomos.backend.repository;

import com.twistedmomos.backend.entity.Order;
import com.twistedmomos.backend.entity.OrderStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** join fetch on a *-to-one (user) is safe with pagination — only a collection fetch join breaks it. */
    @Query(value = "select o from Order o join fetch o.user where o.user.id = :userId",
            countQuery = "select count(o) from Order o where o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "select o from Order o join fetch o.user",
            countQuery = "select count(o) from Order o")
    Page<Order> findAllWithUser(Pageable pageable);

    @Query(value = "select o from Order o join fetch o.user where o.status = :status",
            countQuery = "select count(o) from Order o where o.status = :status")
    Page<Order> findByStatusWithUser(@Param("status") OrderStatus status, Pageable pageable);

    /** Detail loads: items is a collection fetch join, fine here since these return a single result, never a Page. */
    @Query("select o from Order o join fetch o.user left join fetch o.items i left join fetch i.menuItem where o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("select o from Order o join fetch o.user left join fetch o.items i left join fetch i.menuItem "
            + "where o.id = :id and o.user.id = :userId")
    Optional<Order> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);
}
