package com.twistedmomos.backend.auth.service;

import com.twistedmomos.backend.auth.entity.AuthProvider;
import com.twistedmomos.backend.auth.entity.Role;
import com.twistedmomos.backend.auth.entity.RoleName;
import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.auth.entity.UserIdentity;
import com.twistedmomos.backend.auth.exception.UnverifiedLocalAccountException;
import com.twistedmomos.backend.auth.repository.RoleRepository;
import com.twistedmomos.backend.auth.repository.UserIdentityRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import com.twistedmomos.backend.auth.security.GoogleIdentity;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves a verified Google identity to one of our users, creating or linking as needed.
 *
 * <p>The linking rule is the security-sensitive part. Matching on email alone would let an
 * attacker register a victim's address with a password, never verify it, and then inherit
 * the account the moment the real owner signs in with Google. So a link requires the local
 * account to have proved ownership of that address first.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSignInService {

    private final UserIdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public User resolve(GoogleIdentity identity) {
        var existing = identityRepository.findByProviderAndSubject(AuthProvider.GOOGLE, identity.subject());
        if (existing.isPresent()) {
            User user = existing.get().getUser();
            log.info("Google sign-in: existing identity, userId={}", user.getId());
            return user;
        }

        return userRepository.findByEmail(identity.email())
                .map(local -> link(local, identity))
                .orElseGet(() -> create(identity));
    }

    private User link(User local, GoogleIdentity identity) {
        if (!local.isEmailVerified()) {
            log.warn("Google sign-in refused — local account has not confirmed that address: userId={}",
                    local.getId());
            throw new UnverifiedLocalAccountException(
                    "An account already uses this email. Confirm it from your inbox first, then sign in with Google.");
        }
        identityRepository.save(newIdentity(local, identity));
        log.info("Google sign-in: linked to existing account, userId={}", local.getId());
        return local;
    }

    private User create(GoogleIdentity identity) {
        Role customer = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role is not seeded — check V2 migration"));

        User user = userRepository.save(User.builder()
                .name(identity.name() == null ? identity.email() : identity.name())
                .email(identity.email())
                // No password: this account signs in through Google. Password login
                // rejects a null hash, so it cannot be used as a way in.
                .password(null)
                .roles(new HashSet<>(Set.of(customer)))
                .enabled(true)
                // Google already proved the address, so no verification email is needed.
                .emailVerified(true)
                .build());

        identityRepository.save(newIdentity(user, identity));
        log.info("Google sign-in: created account, userId={}", user.getId());
        return user;
    }

    private UserIdentity newIdentity(User user, GoogleIdentity identity) {
        return UserIdentity.builder()
                .user(user)
                .provider(AuthProvider.GOOGLE)
                .providerUserId(identity.subject())
                .email(identity.email())
                .linkedAt(Instant.now())
                .build();
    }
}
