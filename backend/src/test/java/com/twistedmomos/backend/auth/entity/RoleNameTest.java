package com.twistedmomos.backend.auth.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoleNameTest {

    @Test
    void definesTheThreeRolesTheSystemGrants() {
        assertThat(RoleName.values())
                .containsExactlyInAnyOrder(
                        RoleName.CUSTOMER, RoleName.ADMIN, RoleName.RESTAURANT_EMP);
    }
}
