package com.imbuy.notification.infrastructure.persistence;

import com.imbuy.notification.application.port.out.NotificationPersistencePort;
import com.imbuy.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationPersistencePort {

    private final JpaNotificationRepository repository;

    @Override
    public Notification save(Notification notification) {
        return repository.save(notification);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<Notification> findByUserId(Long userId, Pageable pageable) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    @Override
    public Page<Notification> findAll(Pageable pageable) { // ← ДОБАВИТЬ ЭТОТ МЕТОД
        return repository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public List<Notification> findUnreadByUserId(Long userId) {
        return repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public Long countUnreadByUserId(Long userId) {
        return repository.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Long id) {
        repository.markAsRead(id);
    }

    @Override
    public void markAllAsRead(Long userId) {
        repository.markAllAsRead(userId);
    }
}

