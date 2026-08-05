package com.twistedmomos.backend.notification.stream;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseHubTest {

    /**
     * connectionCount() alone doesn't prove delivery — a broadcast that silently no-ops
     * would leave the count unchanged too. Count each emitter's own received sends instead.
     */
    @Test
    void broadcastReachesEveryConnectedOperator() throws Exception {
        SseHub hub = new SseHub();
        AtomicInteger firstReceived = new AtomicInteger();
        AtomicInteger secondReceived = new AtomicInteger();

        SseEmitter first = countingEmitter(firstReceived);
        SseEmitter second = countingEmitter(secondReceived);
        hub.register(first);
        hub.register(second);

        hub.publish(42L);

        assertThat(firstReceived.get()).isEqualTo(1);
        assertThat(secondReceived.get()).isEqualTo(1);
        assertThat(hub.connectionCount()).isEqualTo(2);
    }

    /**
     * A browser that closed without a clean disconnect throws on send. That emitter must be
     * dropped and the others still served — one dead tab cannot silence the kitchen.
     */
    @Test
    void aFailingEmitterIsRemovedAndDoesNotBlockTheRest() throws Exception {
        SseHub hub = new SseHub();
        AtomicInteger healthyReceived = new AtomicInteger();
        SseEmitter healthy = countingEmitter(healthyReceived);
        SseEmitter broken = new SseEmitter() {
            @Override
            public void send(SseEventBuilder builder) throws IOException {
                throw new IOException("client gone");
            }
        };
        hub.register(healthy);
        hub.register(broken);

        assertThat(hub.connectionCount()).isEqualTo(2);
        hub.publish(42L);

        assertThat(hub.connectionCount()).isEqualTo(1);
        assertThat(healthyReceived.get()).isEqualTo(1);
    }

    @Test
    void publishingWithNoOperatorsConnectedIsHarmless() {
        SseHub hub = new SseHub();
        hub.publish(42L);
        assertThat(hub.connectionCount()).isZero();
    }

    /** Real SseEmitter with send() overridden to count instead of writing to an HTTP response. */
    private SseEmitter countingEmitter(AtomicInteger received) {
        return new SseEmitter() {
            @Override
            public void send(SseEventBuilder builder) {
                received.incrementAndGet();
            }
        };
    }
}
