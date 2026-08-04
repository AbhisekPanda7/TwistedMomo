package com.twistedmomos.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The link rule decides whether a Google sign-in can take over an existing account, so each
 * branch is pinned here.
 */
@ExtendWith(MockitoExtension.class)
class GoogleSignInServiceTest {

    private static final GoogleIdentity IDENTITY =
            new GoogleIdentity("google-subject-123", "someone@gmail.com", "Someone");
    private static final Role CUSTOMER = Role.builder().id(1L).name(RoleName.CUSTOMER).build();

    @Mock private UserIdentityRepository identityRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks private GoogleSignInService service;

    @Test
    void signsInAnAlreadyLinkedIdentityWithoutTouchingAnythingElse() {
        User linked = verifiedUser(7L);
        when(identityRepository.findByProviderAndSubject(AuthProvider.GOOGLE, IDENTITY.subject()))
                .thenReturn(Optional.of(UserIdentity.builder().id(1L).user(linked).build()));

        assertThat(service.resolve(IDENTITY)).isSameAs(linked);
        verify(userRepository, never()).save(any());
        verify(identityRepository, never()).save(any());
    }

    /**
     * The takeover this guards: an attacker registers the victim's address, never confirms
     * it, and would otherwise inherit the account when the real owner signs in with Google.
     */
    @Test
    void refusesToLinkWhenTheLocalAccountNeverConfirmedTheAddress() {
        User unverified = verifiedUser(3L);
        unverified.setEmailVerified(false);
        when(identityRepository.findByProviderAndSubject(any(), anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(IDENTITY.email())).thenReturn(Optional.of(unverified));

        assertThatThrownBy(() -> service.resolve(IDENTITY))
                .isInstanceOf(UnverifiedLocalAccountException.class);
        verify(identityRepository, never()).save(any());
    }

    @Test
    void linksToAnExistingAccountThatConfirmedTheAddress() {
        User verified = verifiedUser(5L);
        when(identityRepository.findByProviderAndSubject(any(), anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(IDENTITY.email())).thenReturn(Optional.of(verified));

        assertThat(service.resolve(IDENTITY)).isSameAs(verified);

        ArgumentCaptor<UserIdentity> saved = ArgumentCaptor.forClass(UserIdentity.class);
        verify(identityRepository).save(saved.capture());
        assertThat(saved.getValue().getProviderUserId()).isEqualTo(IDENTITY.subject());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createsAVerifiedPasswordlessAccountWhenNothingMatches() {
        when(identityRepository.findByProviderAndSubject(any(), anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(IDENTITY.email())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(CUSTOMER));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User created = service.resolve(IDENTITY);

        // Google already proved the address, so no verification email is owed.
        assertThat(created.isEmailVerified()).isTrue();
        // Passwordless by construction — password login rejects a null hash.
        assertThat(created.getPassword()).isNull();
        assertThat(created.getRoles()).containsExactly(CUSTOMER);
        verify(identityRepository).save(any(UserIdentity.class));
    }

    private User verifiedUser(Long id) {
        return User.builder()
                .id(id)
                .name("Someone")
                .email(IDENTITY.email())
                .roles(Set.of(CUSTOMER))
                .emailVerified(true)
                .build();
    }
}
