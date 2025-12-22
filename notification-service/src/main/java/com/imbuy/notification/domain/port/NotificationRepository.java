package com.imbuy.notification.domain.port;

import com.imbuy.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    List<Notification> findUnreadByUserId(Long userId);
    Long countUnreadByUserId(Long userId);
    void markAsRead(Long id);
    void markAllAsRead(Long userId);
}

