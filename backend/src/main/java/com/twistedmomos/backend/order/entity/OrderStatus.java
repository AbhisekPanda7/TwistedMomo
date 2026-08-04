package com.twistedmomos.backend.order.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * Order lifecycle, and the only place that says which moves are legal.
 *
 * <p>The transitions live here rather than in a service because they are a property of the
 * states themselves — a second caller keeping its own copy is how the two drift apart.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    /** Past PENDING the kitchen has committed, so only an admin can cancel. */
    public boolean isCancellableByCustomer() {
        return this == PENDING;
    }

    public boolean canTransitionTo(OrderStatus next) {
        return allowedNext().contains(next);
    }

    public Set<OrderStatus> allowedNext() {
        // A switch rather than a static Map: a new constant becomes a compile error here
        // instead of silently getting an empty transition set.
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(PREPARING, CANCELLED);
            case PREPARING -> EnumSet.of(READY, CANCELLED);
            case READY -> EnumSet.of(OUT_FOR_DELIVERY, CANCELLED);
            case OUT_FOR_DELIVERY -> EnumSet.of(DELIVERED);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }
}
