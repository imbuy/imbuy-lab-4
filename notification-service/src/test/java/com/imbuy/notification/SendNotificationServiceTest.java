package com.imbuy.notification;

import com.imbuy.notification.application.dto.NotificationDto;
import com.imbuy.notification.application.mapper.NotificationMapper;
import com.imbuy.notification.application.port.out.NotificationPersistencePort;
import com.imbuy.notification.application.port.out.WebSocketNotificationPort;
import com.imbuy.notification.application.service.SendNotificationService;
import com.imbuy.notification.domain.model.Notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {

    @Mock
    private NotificationPersistencePort persistencePort;

    @Mock
    private WebSocketNotificationPort webSocketPort;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private SendNotificationService service;

    @Test
    void sendWebSocketNotification_savesAndSends() {
        Notification saved = Notification.builder()
                .id(1L)
                .userId(10L)
                .type("WEBSOCKET")
                .title("Title")
                .message("Message")
                .build();

        NotificationDto dto = NotificationDto.builder()
                .id(1L)
                .build();

        when(persistencePort.save(any())).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(dto);

        NotificationDto result =
                service.sendNotification(10L, "WEBSOCKET", "Title", "Message");

        assertEquals(1L, result.getId());
        verify(webSocketPort).sendToUser(10L, dto);
    }

    @Test
    void sendNonWebSocketNotification_doesNotSendWs() {
        when(persistencePort.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any()))
                .thenReturn(new NotificationDto());

        service.sendNotification(10L, "EMAIL", "Title", "Message");

        verifyNoInteractions(webSocketPort);
    }
}
