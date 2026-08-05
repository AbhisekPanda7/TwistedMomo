package com.twistedmomos.backend.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_audit")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAudit {

    public enum Action {
        GRANT,
        REVOKE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "role_name", nullable = false, length = 30)
    private String roleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Action action;

    // Audit row is written once and never updated, so no updated_at. created_at is set
    // by the database default (not set by Hibernate).
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
