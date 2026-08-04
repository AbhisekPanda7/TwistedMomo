package com.twistedmomos.backend.order.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
