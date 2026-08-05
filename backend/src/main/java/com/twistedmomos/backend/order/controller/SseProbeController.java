package com.twistedmomos.backend.order.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * TEMPORARY. Answers one question: does Cloudflare + cloudflared stream text/event-stream
 * through to the browser, or buffer it? Delete once the transport decision is made —
 * the revert commit is already written.
 */
@Tag(name = "Admin — SSE probe", description = "Temporary transport test (ROLE_ADMIN)")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/sse-probe")
public class SseProbeController {

    private static final int TICKS = 180;
    private static final Duration TIMEOUT = Duration.ofMinutes(5);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sse-probe");
        t.setDaemon(true);
        return t;
    });

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(TIMEOUT.toMillis());
        AtomicInteger tick = new AtomicInteger();
        log.info("SSE probe opened");

        var task = scheduler.scheduleAtFixedRate(() -> {
            int n = tick.incrementAndGet();
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(n))
                        .name("tick")
                        .data(n + " " + Instant.now()));
                if (n >= TICKS) {
                    emitter.complete();
                }
            } catch (IOException | IllegalStateException e) {
                // Client hung up, or the emitter already completed. Either way, stop.
                log.info("SSE probe closed at tick {}: {}", n, e.getClass().getSimpleName());
                emitter.completeWithError(e);
            }
        }, 0, 1, TimeUnit.SECONDS);

        // Without this the scheduled task outlives the connection and leaks a thread per probe.
        emitter.onCompletion(() -> task.cancel(true));
        emitter.onTimeout(() -> task.cancel(true));
        emitter.onError(e -> task.cancel(true));

        return emitter;
    }
}
