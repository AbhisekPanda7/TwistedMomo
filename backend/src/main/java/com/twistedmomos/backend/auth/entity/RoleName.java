package com.twistedmomos.backend.auth.entity;

public enum RoleName {
    CUSTOMER(0),
    ADMIN(100);

    /** Higher wins when collapsing a role set to the single legacy `role` field. */
    private final int precedence;

    RoleName(int precedence) {
        this.precedence = precedence;
    }

    public int precedence() {
        return precedence;
    }
}
