package com.imbuy.notification.application.port.in;

public interface MarkNotificationAsReadUseCase {
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
}

