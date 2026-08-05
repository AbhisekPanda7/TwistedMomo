package com.twistedmomos.backend.notification.controller;

import com.twistedmomos.backend.auth.security.CustomUserDetails;
import com.twistedmomos.backend.notification.dto.response.NotificationResponse;
import com.twistedmomos.backend.notification.service.NotificationService;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Every endpoint here operates on the authenticated caller's own notifications — there is no notification-by-id lookup across users. */
@Tag(name = "Notifications", description = "The customer's in-app feed")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(principal.getUser().getId(), pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                Map.of("count", notificationService.unreadCount(principal.getUser().getId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(principal.getUser().getId(), id));
    }
}
