package com.twistedmomos.backend.auth.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.twistedmomos.backend.auth.security.CustomUserDetails;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

/**
 * Staff hold two roles — kitchen or delivery people also order lunch. Authorization must
 * see every one of them, while the singular `role` kept for existing clients resolves to
 * the most privileged.
 */
class UserRolesTest {

    private static final Role CUSTOMER = Role.builder().id(1L).name(RoleName.CUSTOMER).build();
    private static final Role ADMIN = Role.builder().id(2L).name(RoleName.ADMIN).build();

    private static User userWith(Role... roles) {
        return User.builder().id(1L).name("Tester").email("t@example.com").roles(Set.of(roles)).build();
    }

    @Test
    void grantsAnAuthorityForEveryRole() {
        var authorities = new CustomUserDetails(userWith(CUSTOMER, ADMIN)).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_CUSTOMER", "ROLE_ADMIN");
    }

    @Test
    void primaryRoleIsTheMostPrivilegedOne() {
        assertThat(userWith(CUSTOMER, ADMIN).primaryRole()).isEqualTo(RoleName.ADMIN);
        assertThat(userWith(CUSTOMER).primaryRole()).isEqualTo(RoleName.CUSTOMER);
    }

    /** hasRole answers for any held role, not just the primary one. */
    @Test
    void hasRoleSeesEveryHeldRole() {
        User staff = userWith(CUSTOMER, ADMIN);

        assertThat(staff.hasRole(RoleName.ADMIN)).isTrue();
        assertThat(staff.hasRole(RoleName.CUSTOMER)).isTrue();
        assertThat(userWith(CUSTOMER).hasRole(RoleName.ADMIN)).isFalse();
    }
}
