package com.twistedmomos.backend.notification.controller;

import com.twistedmomos.backend.notification.stream.SseHub;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * The stream carries a signal, not state: the client refetches the order list on connect and
 * on every event, so a gap heals itself and no replay buffer is needed.
 * ROLE_ADMIN or ROLE_RESTAURANT_EMP — enforced in SecurityConfig via the /api/v1/ops/** matcher.
 */
@Tag(name = "Ops — Stream", description = "Live order signal (ROLE_ADMIN, ROLE_RESTAURANT_EMP)")
@RestController
@RequestMapping("/api/v1/ops/stream")
@RequiredArgsConstructor
public class OpsStreamController {

    private final SseHub sseHub;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseHub.subscribe();
    }
}
