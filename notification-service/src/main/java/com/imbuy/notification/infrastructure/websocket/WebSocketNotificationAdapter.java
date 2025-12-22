package com.imbuy.notification.infrastructure.websocket;

import com.imbuy.notification.application.dto.NotificationDto;
import com.imbuy.notification.application.port.out.WebSocketNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotificationAdapter implements WebSocketNotificationPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendToUser(Long userId, NotificationDto notification) {
        String destination = "/topic/user/" + userId + "/notifications";
        log.info("Sending WebSocket notification to user {} via {}", userId, destination);
        messagingTemplate.convertAndSend(destination, notification);
    }

    @Override
    public void sendToAll(NotificationDto notification) {
        log.info("Broadcasting WebSocket notification to all users");
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}

