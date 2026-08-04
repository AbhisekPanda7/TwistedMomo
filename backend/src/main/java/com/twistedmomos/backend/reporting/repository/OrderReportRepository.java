package com.twistedmomos.backend.reporting.repository;

import com.twistedmomos.backend.reporting.entity.OrderReportLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReportRepository extends JpaRepository<OrderReportLine, Long> {

    /** The outbox retries a failed listener, so a replay must not double-count. */
    boolean existsByOrderId(Long orderId);
}
