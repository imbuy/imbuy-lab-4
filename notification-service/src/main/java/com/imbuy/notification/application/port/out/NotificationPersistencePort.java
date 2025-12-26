package com.imbuy.notification.application.port.out;

import com.imbuy.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NotificationPersistencePort {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    List<Notification> findUnreadByUserId(Long userId);
    Long countUnreadByUserId(Long userId);
    void markAsRead(Long id);
    void markAllAsRead(Long userId);
    Page<Notification> findAll(Pageable pageable);
}

