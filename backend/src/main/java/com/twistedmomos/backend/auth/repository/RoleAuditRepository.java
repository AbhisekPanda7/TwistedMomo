package com.twistedmomos.backend.auth.repository;

import com.twistedmomos.backend.auth.entity.RoleAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleAuditRepository extends JpaRepository<RoleAudit, Long> {}
