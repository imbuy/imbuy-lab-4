package com.imbuy.notification;

import com.imbuy.notification.application.port.out.NotificationPersistencePort;
import com.imbuy.notification.application.service.MarkNotificationAsReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MarkNotificationAsReadServiceTest {

    @Mock
    private NotificationPersistencePort persistencePort;

    @InjectMocks
    private MarkNotificationAsReadService service;

    @Test
    void markAsRead_callsPort() {
        service.markAsRead(1L);
        verify(persistencePort).markAsRead(1L);
    }

    @Test
    void markAllAsRead_callsPort() {
        service.markAllAsRead(10L);
        verify(persistencePort).markAllAsRead(10L);
    }
}
