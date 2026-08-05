package com.twistedmomos.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.auth.entity.Role;
import com.twistedmomos.backend.auth.entity.RoleAudit;
import com.twistedmomos.backend.auth.entity.RoleName;
import com.twistedmomos.backend.auth.entity.User;
import com.twistedmomos.backend.auth.exception.InvalidRoleException;
import com.twistedmomos.backend.auth.exception.LastAdminException;
import com.twistedmomos.backend.auth.exception.RoleNotGrantedException;
import com.twistedmomos.backend.auth.mapper.UserAdminMapper;
import com.twistedmomos.backend.auth.repository.RoleAuditRepository;
import com.twistedmomos.backend.auth.repository.RoleRepository;
import com.twistedmomos.backend.auth.repository.UserRepository;
import com.twistedmomos.backend.auth.service.impl.UserAdminServiceImpl;
import com.twistedmomos.backend.shared.exception.ResourceNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long SUBJECT_ID = 2L;

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RoleAuditRepository roleAuditRepository;
    @Mock private UserAdminMapper userAdminMapper;

    private UserAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserAdminServiceImpl(
                userRepository, roleRepository, roleAuditRepository, userAdminMapper);
    }

    private static Role role(RoleName name) {
        Role r = new Role();
        r.setId((long) name.ordinal() + 1);
        r.setName(name);
        return r;
    }

    private static User userWith(Long id, RoleName... names) {
        Set<Role> roles = new HashSet<>();
        for (RoleName n : names) {
            roles.add(role(n));
        }
        return User.builder().id(id).email("u" + id + "@test.local").roles(roles).build();
    }

    @Test
    void grantAddsTheRoleAndWritesAnAuditRow() {
        User subject = userWith(SUBJECT_ID, RoleName.CUSTOMER);
        when(userRepository.findByIdWithRoles(SUBJECT_ID)).thenReturn(Optional.of(subject));
        when(roleRepository.findByName(RoleName.RESTAURANT_EMP))
                .thenReturn(Optional.of(role(RoleName.RESTAURANT_EMP)));

        service.grant(ACTOR_ID, SUBJECT_ID, "RESTAURANT_EMP");

        assertThat(subject.hasRole(RoleName.RESTAURANT_EMP)).isTrue();

        ArgumentCaptor<RoleAudit> audit = ArgumentCaptor.forClass(RoleAudit.class);
        verify(roleAuditRepository).save(audit.capture());
        assertThat(audit.getValue().getActorId()).isEqualTo(ACTOR_ID);
        assertThat(audit.getValue().getSubjectId()).isEqualTo(SUBJECT_ID);
        assertThat(audit.getValue().getRoleName()).isEqualTo("RESTAURANT_EMP");
        assertThat(audit.getValue().getAction()).isEqualTo(RoleAudit.Action.GRANT);
    }

    /** Idempotent by design: the UI may double-submit, and a duplicate is not an error. */
    @Test
    void grantingAHeldRoleChangesNothingAndWritesNoAudit() {
        User subject = userWith(SUBJECT_ID, RoleName.CUSTOMER);
        when(userRepository.findByIdWithRoles(SUBJECT_ID)).thenReturn(Optional.of(subject));

        service.grant(ACTOR_ID, SUBJECT_ID, "CUSTOMER");

        assertThat(subject.getRoles()).hasSize(1);
        verify(roleAuditRepository, never()).save(any());
    }

    @Test
    void revokeRemovesTheRoleAndWritesAnAuditRow() {
        User subject = userWith(SUBJECT_ID, RoleName.CUSTOMER, RoleName.RESTAURANT_EMP);
        when(userRepository.findByIdWithRoles(SUBJECT_ID)).thenReturn(Optional.of(subject));

        service.revoke(ACTOR_ID, SUBJECT_ID, "RESTAURANT_EMP");

        assertThat(subject.hasRole(RoleName.RESTAURANT_EMP)).isFalse();
        ArgumentCaptor<RoleAudit> audit = ArgumentCaptor.forClass(RoleAudit.class);
        verify(roleAuditRepository).save(audit.capture());
        assertThat(audit.getValue().getAction()).isEqualTo(RoleAudit.Action.REVOKE);
    }

    @Test
    void revokingARoleTheUserDoesNotHoldIsRejected() {
        User subject = userWith(SUBJECT_ID, RoleName.CUSTOMER);
        when(userRepository.findByIdWithRoles(SUBJECT_ID)).thenReturn(Optional.of(subject));

        assertThatThrownBy(() -> service.revoke(ACTOR_ID, SUBJECT_ID, "ADMIN"))
                .isInstanceOf(RoleNotGrantedException.class);

        verify(roleAuditRepository, never()).save(any());
    }

    /**
     * The guard that matters: losing the last admin locks everyone out of role
     * management with no recovery short of database access.
     */
    @Test
    void revokingTheOnlyAdminIsRejected() {
        User subject = userWith(SUBJECT_ID, RoleName.ADMIN);
        when(userRepository.findByIdWithRoles(SUBJECT_ID)).thenReturn(Optional.of(subject));
        when(userRepository.findAndLockByRoleName(RoleName.ADMIN)).thenReturn(List.of(subject));

        assertThatThrownBy(() -> service.revoke(ACTOR_ID, SUBJECT_ID, "ADMIN"))
                .isInstanceOf(LastAdminException.class);

        assertThat(subject.hasRole(RoleName.ADMIN)).isTrue();
        verify(roleAuditRepository, never()).save(any());
    }

    @Test
    void revokingOneOfTwoAdminsIsAllowed() {
        User subject = userWith(SUBJECT_ID, RoleName.ADMIN);
        when(userRepository.findByIdWithRoles(SUBJECT_ID)).thenReturn(Optional.of(subject));
        when(userRepository.findAndLockByRoleName(RoleName.ADMIN))
                .thenReturn(List.of(subject, userWith(99L, RoleName.ADMIN)));

        service.revoke(ACTOR_ID, SUBJECT_ID, "ADMIN");

        assertThat(subject.hasRole(RoleName.ADMIN)).isFalse();
    }

    /** The rule is about the system keeping an admin, not about who is asking. */
    @Test
    void anAdminMayRevokeTheirOwnAdminRoleWhileAnotherRemains() {
        User self = userWith(ACTOR_ID, RoleName.ADMIN);
        when(userRepository.findByIdWithRoles(ACTOR_ID)).thenReturn(Optional.of(self));
        when(userRepository.findAndLockByRoleName(RoleName.ADMIN))
                .thenReturn(List.of(self, userWith(99L, RoleName.ADMIN)));

        service.revoke(ACTOR_ID, ACTOR_ID, "ADMIN");

        assertThat(self.hasRole(RoleName.ADMIN)).isFalse();
    }

    @Test
    void anUnknownRoleNameIsRejected() {
        assertThatThrownBy(() -> service.grant(ACTOR_ID, SUBJECT_ID, "WIZARD"))
                .isInstanceOf(InvalidRoleException.class);
    }

    @Test
    void anUnknownUserIsNotFound() {
        when(userRepository.findByIdWithRoles(SUBJECT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.grant(ACTOR_ID, SUBJECT_ID, "ADMIN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
