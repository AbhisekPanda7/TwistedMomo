package com.twistedmomos.backend.notification.stream;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Holds the operator connections and pushes a signal to them.
 *
 * <p>In-memory and per-instance by design, like LoginRateLimiter. An SseEmitter is a live
 * HTTP connection owned by one JVM and can never move to Redis; what E7 adds is pub/sub
 * between instances, each still pushing to its own local emitters.
 */
@Slf4j
@Component
public class SseHub implements OrderEventBroadcaster {

    private static final Duration TIMEOUT = Duration.ofHours(2);
    private static final Duration HEARTBEAT = Duration.ofSeconds(20);

    // CopyOnWriteArrayList: emitters are added by request threads, removed by callback
    // and heartbeat threads, and iterated during broadcast — removal mid-iteration is safe here.
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService heartbeat =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sse-heartbeat");
                t.setDaemon(true);
                return t;
            });

    public SseHub() {
        // A quiet kitchen must not look like a dead connection to any intermediary.
        heartbeat.scheduleAtFixedRate(this::ping,
                HEARTBEAT.toSeconds(), HEARTBEAT.toSeconds(), TimeUnit.SECONDS);
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT.toMillis());
        register(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        log.info("Operator connected: connections={}", emitters.size());
        return emitter;
    }

    @Override
    public void publish(Long orderId) {
        send(SseEmitter.event().name("order").data(String.valueOf(orderId)));
    }

    private void ping() {
        send(SseEmitter.event().comment("keepalive"));
    }

    private void send(SseEmitter.SseEventBuilder event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (IOException | IllegalStateException e) {
                // The client is gone, or the emitter already completed. Nothing to retry.
                emitters.remove(emitter);
                log.debug("Dropped a dead emitter: {}", e.getClass().getSimpleName());
            }
        }
    }

    @PreDestroy
    void shutdown() {
        heartbeat.shutdownNow();
    }

    /** Package-private for tests only — lets a test inject an emitter that fails on send. */
    void register(SseEmitter emitter) {
        emitters.add(emitter);
    }

    /** Package-private for tests only. */
    int connectionCount() {
        return emitters.size();
    }
}
