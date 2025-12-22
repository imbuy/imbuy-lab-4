package com.imbuy.notification.application.service;

import com.imbuy.notification.application.port.in.MarkNotificationAsReadUseCase;
import com.imbuy.notification.application.port.out.NotificationPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkNotificationAsReadService implements MarkNotificationAsReadUseCase {

    private final NotificationPersistencePort persistencePort;

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        persistencePort.markAsRead(notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        persistencePort.markAllAsRead(userId);
    }
}

