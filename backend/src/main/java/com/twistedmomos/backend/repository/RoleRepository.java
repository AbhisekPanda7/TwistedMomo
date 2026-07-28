package com.twistedmomos.backend.repository;

import com.twistedmomos.backend.entity.Role;
import com.twistedmomos.backend.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
