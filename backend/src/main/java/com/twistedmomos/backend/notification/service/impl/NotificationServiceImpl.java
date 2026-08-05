package com.twistedmomos.backend.notification.service.impl;

import com.twistedmomos.backend.notification.dto.response.NotificationResponse;
import com.twistedmomos.backend.notification.entity.Notification;
import com.twistedmomos.backend.notification.mapper.NotificationMapper;
import com.twistedmomos.backend.notification.repository.NotificationRepository;
import com.twistedmomos.backend.notification.service.NotificationService;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import com.twistedmomos.backend.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public PageResponse<NotificationResponse> list(Long userId, Pageable pageable) {
        return PageResponse.of(notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse));
    }

    @Override
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Long userId, Long notificationId) {
        // Scoped by userId: a foreign notification answers 404, never 403.
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> {
                    log.warn("Notification not found or not owned: id={} userId={}",
                            notificationId, userId);
                    return new ResourceNotFoundException(
                            "Notification " + notificationId + " not found");
                });

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
        }
        return notificationMapper.toResponse(notification);
    }
}
