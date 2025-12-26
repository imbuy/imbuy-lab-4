package com.imbuy.notification;

import com.imbuy.notification.application.dto.NotificationDto;
import com.imbuy.notification.application.port.in.GetNotificationsUseCase;
import com.imbuy.notification.application.service.SendNotificationService;
import com.imbuy.notification.domain.model.Notification;
import com.imbuy.notification.application.port.out.WebSocketNotificationPort;
import com.imbuy.notification.application.mapper.NotificationMapper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class NotificationServiceApplicationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private SendNotificationService sendNotificationService;

    @Autowired
    private GetNotificationsUseCase getNotificationsUseCase;

    @MockBean
    private WebSocketNotificationPort webSocketPort;

    @MockBean
    private NotificationMapper mapper;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        kafka.start();
    }

    @AfterAll
    static void afterAll() {
        kafka.stop();
        postgres.stop();
    }

    @Test
    void shouldSendWebSocketNotification() {
        Notification notification = Notification.builder()
                .userId(10L)
                .type("WEBSOCKET")
                .title("Test Title")
                .message("Test Message")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationDto dto = NotificationDto.builder()
                .userId(10L)
                .title("Test Title")
                .message("Test Message")
                .read(false)
                .build();

        when(mapper.toDto(any(Notification.class))).thenReturn(dto);

        NotificationDto result = sendNotificationService.sendNotification(10L, "WEBSOCKET", "Test Title", "Test Message");

        assertThat(result.getTitle()).isEqualTo("Test Title");
        verify(webSocketPort).sendToUser(eq(10L), any(NotificationDto.class));
    }


    @Test
    void shouldGetNotificationsPage_threeNotifications() {
        NotificationDto dto1 = NotificationDto.builder().id(1L).userId(10L).title("Title 1").message("Message 1").read(false).build();

        when(mapper.toDto(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            if (n.getTitle().equals("Title 1")) return dto1;
            return null;
        });

        sendNotificationService.sendNotification(10L, "EMAIL", "Title 1", "Message 1");
        Page<NotificationDto> page = getNotificationsUseCase.getNotifications(10L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
    }

}
