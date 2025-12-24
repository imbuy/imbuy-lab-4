package com.imbuy.notification;

import com.imbuy.notification.application.dto.NotificationDto;
import com.imbuy.notification.application.mapper.NotificationMapper;
import com.imbuy.notification.application.port.out.NotificationPersistencePort;
import com.imbuy.notification.application.service.GetNotificationsService;
import com.imbuy.notification.domain.model.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNotificationsServiceTest {

    @Mock
    private NotificationPersistencePort persistencePort;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private GetNotificationsService service;

    @Test
    void getNotifications_mapsEntitiesToDto() {
        Notification entity = Notification.builder()
                .id(1L)
                .userId(10L)
                .title("Title")
                .message("Message")
                .read(false)
                .build();

        NotificationDto dto = NotificationDto.builder()
                .id(1L)
                .title("Title")
                .build();

        Page<Notification> page =
                new PageImpl<>(List.of(entity));

        when(persistencePort.findByUserId(eq(10L), any()))
                .thenReturn(page);
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<NotificationDto> result =
                service.getNotifications(10L, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Title", result.getContent().get(0).getTitle());
    }

    @Test
    void getUnreadCount_returnsValue() {
        when(persistencePort.countUnreadByUserId(10L)).thenReturn(3L);

        Long count = service.getUnreadCount(10L);

        assertEquals(3L, count);
    }
}
