package com.twistedmomos.backend.notification.stream;

/**
 * Fan-out to connected operators. In-memory today; E7 adds a Redis implementation so a
 * second backend instance can reach emitters it does not hold.
 */
public interface OrderEventBroadcaster {
    void publish(Long orderId);
}
