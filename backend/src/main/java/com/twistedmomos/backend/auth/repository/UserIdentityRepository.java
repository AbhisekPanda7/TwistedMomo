package com.twistedmomos.backend.auth.repository;

import com.twistedmomos.backend.auth.entity.AuthProvider;
import com.twistedmomos.backend.auth.entity.UserIdentity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {

    /** Joins the user and their roles: the caller issues a token straight from this. */
    @Query("select i from UserIdentity i join fetch i.user u join fetch u.roles "
            + "where i.provider = :provider and i.providerUserId = :providerUserId")
    Optional<UserIdentity> findByProviderAndSubject(AuthProvider provider, String providerUserId);
}
