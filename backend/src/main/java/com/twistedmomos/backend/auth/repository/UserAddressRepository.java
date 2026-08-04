package com.twistedmomos.backend.auth.repository;

import com.twistedmomos.backend.auth.entity.UserAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    /** The upsert probe: a repeat delivery updates this row rather than adding another. */
    Optional<UserAddress> findByUserIdAndNormalizedKey(Long userId, String normalizedKey);

    /** Pageable carries the cap; the caller asks for the first N. */
    List<UserAddress> findByUserIdOrderByLastUsedAtDesc(Long userId, Pageable pageable);

    /** Scoped by userId so another customer's address is simply not found. */
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);
}
