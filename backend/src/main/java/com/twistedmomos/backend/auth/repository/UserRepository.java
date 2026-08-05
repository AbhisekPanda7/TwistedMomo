package com.twistedmomos.backend.auth.repository;

import com.twistedmomos.backend.auth.entity.RoleName;
import com.twistedmomos.backend.auth.entity.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Fetches roles eagerly in the same query. Roles are required on every
     * authentication check (JWT claims, authorities) and open-in-view is
     * disabled, so a lazy collection here would blow up outside a transaction —
     * see JwtAuthenticationFilter, which is exactly that case.
     */
    @Query("select u from User u join fetch u.roles where u.email = :email")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Just the flag, so callers outside auth can gate on it without loading the user or
     * reaching into the entity.
     */
    @Query("select u.emailVerified from User u where u.id = :userId")
    Optional<Boolean> findEmailVerifiedById(Long userId);

    /**
     * Paginated with a count query: a fetch join makes Hibernate unable to derive
     * the count itself, and without this it would load every row to paginate in memory.
     */
    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles",
           countQuery = "SELECT COUNT(u) FROM User u")
    Page<User> findAllWithRoles(Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    /**
     * Locks the matching rows for the transaction. A plain count would let two
     * concurrent revokes each observe two admins and both proceed, leaving none.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findAndLockByRoleName(@Param("roleName") RoleName roleName);
}
