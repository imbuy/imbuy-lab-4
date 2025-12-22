package com.imbuy.notification.application.port.out;

import com.imbuy.notification.application.dto.NotificationDto;

public interface WebSocketNotificationPort {
    void sendToUser(Long userId, NotificationDto notification);
    void sendToAll(NotificationDto notification);
}

