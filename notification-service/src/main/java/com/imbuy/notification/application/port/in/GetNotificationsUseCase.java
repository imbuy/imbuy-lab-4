package com.imbuy.notification.application.port.in;

import com.imbuy.notification.application.dto.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetNotificationsUseCase {
    Page<NotificationDto> getNotifications(Long userId, Pageable pageable);
    Long getUnreadCount(Long userId);
    Page<NotificationDto> getAllNotifications(Pageable pageable);
}

