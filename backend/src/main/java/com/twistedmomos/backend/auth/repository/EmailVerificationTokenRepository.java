package com.twistedmomos.backend.auth.repository;

import com.twistedmomos.backend.auth.entity.EmailVerificationToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /** Joins the user because verification flips a field on it — see open-in-view=false. */
    @Query("select t from EmailVerificationToken t join fetch t.user where t.tokenHash = :tokenHash")
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    /** Issuing a new link invalidates any earlier one, so a resend cannot leave two live tokens. */
    @Modifying
    @Query("update EmailVerificationToken t set t.usedAt = :now "
            + "where t.user.id = :userId and t.usedAt is null")
    int invalidateOutstanding(Long userId, Instant now);

    long countByUserIdAndCreatedAtAfter(Long userId, Instant since);
}
