package com.imbuy.notification.application.service;

import com.imbuy.notification.application.dto.NotificationDto;
import com.imbuy.notification.application.mapper.NotificationMapper;
import com.imbuy.notification.application.port.in.GetNotificationsUseCase;
import com.imbuy.notification.application.port.out.NotificationPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetNotificationsService implements GetNotificationsUseCase {

    private final NotificationPersistencePort persistencePort;
    private final NotificationMapper mapper;

    @Override
    public Page<NotificationDto> getNotifications(Long userId, Pageable pageable) {
        return persistencePort.findByUserId(userId, pageable)
                .map(mapper::toDto);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return persistencePort.countUnreadByUserId(userId);
    }
}

