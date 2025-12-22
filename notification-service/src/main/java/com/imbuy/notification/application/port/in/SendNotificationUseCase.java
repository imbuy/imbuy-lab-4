package com.imbuy.notification.application.port.in;

import com.imbuy.notification.application.dto.NotificationDto;

public interface SendNotificationUseCase {
    NotificationDto sendNotification(Long userId, String type, String title, String message);
}

