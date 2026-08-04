package com.twistedmomos.backend.auth.repository;

import com.twistedmomos.backend.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
