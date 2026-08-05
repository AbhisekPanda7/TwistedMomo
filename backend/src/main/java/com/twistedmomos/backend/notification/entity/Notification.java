package com.twistedmomos.backend.notification.entity;

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
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Raw id, not a @ManyToOne: an association would pull the auth module's User
    // entity across the boundary and fail Modulith verification.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /** Denormalized text, not a template key: a notification records what the customer was told. */
    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    // Raw id, not a @ManyToOne: keeps order/ out of this module's imports.
    @Column(name = "order_id")
    private Long orderId;

    @Setter
    @Column(name = "read_at")
    private Instant readAt;

    // Written once, never updated by Hibernate; the database default sets it.
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
