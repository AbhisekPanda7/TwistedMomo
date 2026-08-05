package com.twistedmomos.backend.auth.service.impl;

import com.twistedmomos.backend.auth.dto.response.UserAdminResponse;
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
import com.twistedmomos.backend.auth.service.UserAdminService;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import com.twistedmomos.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleAuditRepository roleAuditRepository;
    private final UserAdminMapper userAdminMapper;

    @Override
    public PageResponse<UserAdminResponse> list(Pageable pageable) {
        return PageResponse.of(
                userRepository.findAllWithRoles(pageable).map(userAdminMapper::toResponse));
    }

    @Override
    @Transactional
    public UserAdminResponse grant(Long actorId, Long subjectId, String roleName) {
        RoleName target = parseRole(roleName);
        User subject = loadUser(subjectId);

        if (subject.hasRole(target)) {
            log.info("Role already held, nothing to do: subjectId={} role={}", subjectId, target);
            return userAdminMapper.toResponse(subject);
        }

        Role role = roleRepository.findByName(target)
                .orElseThrow(() -> new ResourceNotFoundException("Role " + target + " not found"));
        subject.getRoles().add(role);
        writeAudit(actorId, subjectId, target, RoleAudit.Action.GRANT);
        log.info("Role granted: actorId={} subjectId={} role={}", actorId, subjectId, target);
        return userAdminMapper.toResponse(subject);
    }

    @Override
    @Transactional
    public UserAdminResponse revoke(Long actorId, Long subjectId, String roleName) {
        RoleName target = parseRole(roleName);
        User subject = loadUser(subjectId);

        if (!subject.hasRole(target)) {
            log.warn("Revoke rejected - role not held: subjectId={} role={}", subjectId, target);
            throw new RoleNotGrantedException("That user does not hold the " + target + " role");
        }

        // Row-locked inside the write transaction so a concurrent revoke cannot also
        // see two admins and leave the system with none.
        if (target == RoleName.ADMIN
                && userRepository.findAndLockByRoleName(RoleName.ADMIN).size() <= 1) {
            log.warn("Revoke rejected - last admin: actorId={} subjectId={}", actorId, subjectId);
            throw new LastAdminException(
                    "This is the only admin left - grant the role to someone else first");
        }

        subject.getRoles().removeIf(r -> r.getName() == target);
        writeAudit(actorId, subjectId, target, RoleAudit.Action.REVOKE);
        log.info("Role revoked: actorId={} subjectId={} role={}", actorId, subjectId, target);
        return userAdminMapper.toResponse(subject);
    }

    private User loadUser(Long id) {
        return userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
    }

    // Parsed before the user lookup so a bad role name never depends on the user existing.
    private RoleName parseRole(String value) {
        try {
            return RoleName.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role requested: value={}", value);
            throw new InvalidRoleException("Unknown role: " + value);
        }
    }

    private void writeAudit(Long actorId, Long subjectId, RoleName role, RoleAudit.Action action) {
        roleAuditRepository.save(RoleAudit.builder()
                .actorId(actorId)
                .subjectId(subjectId)
                .roleName(role.name())
                .action(action)
                .build());
    }
}
