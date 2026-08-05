package com.twistedmomos.backend.notification.repository;

import com.twistedmomos.backend.notification.entity.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndReadAtIsNull(Long userId);

    /** Scoped by userId, not id alone — there is no notification-by-id lookup. */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    boolean existsByOrderIdAndTitle(Long orderId, String title);
}
