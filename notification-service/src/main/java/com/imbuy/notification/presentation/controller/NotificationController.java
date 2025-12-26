package com.imbuy.notification.presentation.controller;

import com.imbuy.notification.application.dto.NotificationDto;
import com.imbuy.notification.application.port.in.GetNotificationsUseCase;
import com.imbuy.notification.application.port.in.MarkNotificationAsReadUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(getNotificationsUseCase.getNotifications(userId, pageable));
    }

    @GetMapping("/users/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(getNotificationsUseCase.getUnreadCount(userId));
    }
    @Operation(summary = "Получить все уведомления")
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getAllNotifications(
            @Parameter(description = "Пагинация и сортировка")
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 50) Pageable pageable) {
        return ResponseEntity.ok(getNotificationsUseCase.getAllNotifications(pageable));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        markNotificationAsReadUseCase.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        markNotificationAsReadUseCase.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}

