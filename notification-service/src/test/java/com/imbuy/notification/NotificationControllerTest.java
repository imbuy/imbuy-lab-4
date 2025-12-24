package com.imbuy.notification;

import com.imbuy.notification.application.dto.NotificationDto;
import com.imbuy.notification.application.port.in.GetNotificationsUseCase;
import com.imbuy.notification.application.port.in.MarkNotificationAsReadUseCase;
import com.imbuy.notification.presentation.controller.NotificationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetNotificationsUseCase getNotificationsUseCase;

    @MockBean
    private MarkNotificationAsReadUseCase markNotificationAsReadUseCase;

    @Test
    void getNotifications_returnsPage() throws Exception {
        NotificationDto dto = NotificationDto.builder()
                .id(1L)
                .userId(10L)
                .title("Test")
                .message("Message")
                .read(false)
                .build();

        Page<NotificationDto> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(getNotificationsUseCase.getNotifications(eq(10L), any()))
                .thenReturn(page);

        mockMvc.perform(get("/notifications/users/10")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test"));
    }

    @Test
    void markAsRead_returnsNoContent() throws Exception {
        mockMvc.perform(put("/notifications/1/read"))
                .andExpect(status().isNoContent());

        verify(markNotificationAsReadUseCase).markAsRead(1L);
    }

    @Test
    void markAllAsRead_returnsNoContent() throws Exception {
        mockMvc.perform(put("/notifications/users/10/read-all"))
                .andExpect(status().isNoContent());

        verify(markNotificationAsReadUseCase).markAllAsRead(10L);
    }
}
