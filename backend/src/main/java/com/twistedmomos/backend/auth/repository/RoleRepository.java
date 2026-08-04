package com.twistedmomos.backend.auth.repository;

import com.twistedmomos.backend.auth.entity.Role;
import com.twistedmomos.backend.auth.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
