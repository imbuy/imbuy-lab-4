package com.imbuy.notification.application.service;

import com.imbuy.notification.application.dto.NotificationDto;
import com.imbuy.notification.application.mapper.NotificationMapper;
import com.imbuy.notification.application.port.in.SendNotificationUseCase;
import com.imbuy.notification.application.port.out.NotificationPersistencePort;
import com.imbuy.notification.application.port.out.WebSocketNotificationPort;
import com.imbuy.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendNotificationService implements SendNotificationUseCase {

    private final NotificationPersistencePort persistencePort;
    private final WebSocketNotificationPort webSocketPort;
    private final NotificationMapper mapper;

    @Override
    @Transactional
    public NotificationDto sendNotification(Long userId, String type, String title, String message) {
        log.info("Sending notification to user {}: type={}, title={}", userId, type, title);

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .build();

        Notification saved = persistencePort.save(notification);
        NotificationDto dto = mapper.toDto(saved);

        // Send via WebSocket if type is WEBSOCKET
        if ("WEBSOCKET".equalsIgnoreCase(type)) {
            webSocketPort.sendToUser(userId, dto);
        }

        return dto;
    }
}

