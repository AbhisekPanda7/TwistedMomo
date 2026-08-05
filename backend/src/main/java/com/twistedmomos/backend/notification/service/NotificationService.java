package com.twistedmomos.backend.notification.service;

import com.twistedmomos.backend.notification.dto.response.NotificationResponse;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    PageResponse<NotificationResponse> list(Long userId, Pageable pageable);

    long unreadCount(Long userId);

    NotificationResponse markRead(Long userId, Long notificationId);
}
