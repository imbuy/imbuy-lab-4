package imbuy.lot;

import imbuy.lot.application.dto.CreateLotDto;
import imbuy.lot.application.dto.LotDto;
import imbuy.lot.application.dto.UpdateLotDto;
import imbuy.lot.application.dto.UserDto;
import imbuy.lot.application.port.out.BidPort;
import imbuy.lot.application.port.out.UserPort;
import imbuy.lot.domain.enums.LotStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class LotServiceApplicationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private imbuy.lot.application.port.in.LotUseCase lotService;

    @MockBean
    private UserPort userPort;

    @MockBean
    private BidPort bidPort;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

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
    void shouldCreateAndRetrieveLot() {
        // Arrange
        Long userId = 1L;
        UserDto userDto = new UserDto(userId, "user@test.com", "testuser", "USER");
        when(userPort.getUserById(userId)).thenReturn(userDto);

        CreateLotDto createDto = new CreateLotDto(
                "Test Lot",
                "Test Description",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7)
        );

        // Act
        LotDto created = lotService.createLot(createDto, userId);

        // Assert
        assertThat(created).isNotNull();
        assertThat(created.title()).isEqualTo("Test Lot");
        assertThat(created.status()).isEqualTo(LotStatus.PENDING_APPROVAL);
        assertThat(created.start_price()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldApproveLotWithModeratorRole() {
        // Arrange
        Long userId = 1L;
        Long moderatorId = 2L;

        UserDto userDto = new UserDto(userId, "user@test.com", "testuser", "USER");
        UserDto moderatorDto = new UserDto(moderatorId, "mod@test.com", "moderator", "MODERATOR");

        when(userPort.getUserById(userId)).thenReturn(userDto);
        when(userPort.getUserById(moderatorId)).thenReturn(moderatorDto);

        CreateLotDto createDto = new CreateLotDto(
                "Test Lot",
                "Test Description",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7)
        );

        LotDto created = lotService.createLot(createDto, userId);

        // Act
        LotDto approved = lotService.approveLot(created.id(), moderatorId);

        // Assert
        assertThat(approved.status()).isEqualTo(LotStatus.ACTIVE);
    }

    @Test
    void shouldThrowExceptionWhenNonModeratorApproves() {
        // Arrange
        Long userId = 1L;
        Long regularUserId = 3L;

        UserDto userDto = new UserDto(userId, "user@test.com", "testuser", "USER");
        UserDto regularUserDto = new UserDto(regularUserId, "regular@test.com", "regular", "USER");

        when(userPort.getUserById(userId)).thenReturn(userDto);
        when(userPort.getUserById(regularUserId)).thenReturn(regularUserDto);

        CreateLotDto createDto = new CreateLotDto(
                "Test Lot",
                "Test Description",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7)
        );

        LotDto created = lotService.createLot(createDto, userId);

        // Act & Assert
        assertThatThrownBy(() -> lotService.approveLot(created.id(), regularUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User has no permission to approve lot");
    }

    @Test
    void shouldUpdateLotByOwner() {
        // Arrange
        Long userId = 1L;
        UserDto userDto = new UserDto(userId, "user@test.com", "testuser", "USER");
        when(userPort.getUserById(userId)).thenReturn(userDto);

        CreateLotDto createDto = new CreateLotDto(
                "Test Lot",
                "Test Description",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7)
        );

        LotDto created = lotService.createLot(createDto, userId);

        UpdateLotDto updateDto = new UpdateLotDto(
                "Updated Title",
                "Updated Description",
                new BigDecimal("15.00"),
                2L,
                LocalDateTime.now().plusDays(10)
        );

        // Act
        LotDto updated = lotService.updateLot(created.id(), updateDto, userId);

        // Assert
        assertThat(updated.title()).isEqualTo("Updated Title");
        assertThat(updated.description()).isEqualTo("Updated Description");
        assertThat(updated.bid_step()).isEqualByComparingTo("15.00");
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerUpdates() {
        // Arrange
        Long ownerId = 1L;
        Long otherUserId = 2L;

        UserDto ownerDto = new UserDto(ownerId, "owner@test.com", "owner", "USER");
        UserDto otherUserDto = new UserDto(otherUserId, "other@test.com", "other", "USER");

        when(userPort.getUserById(ownerId)).thenReturn(ownerDto);
        when(userPort.getUserById(otherUserId)).thenReturn(otherUserDto);

        CreateLotDto createDto = new CreateLotDto(
                "Test Lot",
                "Test Description",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7)
        );

        LotDto created = lotService.createLot(createDto, ownerId);

        UpdateLotDto updateDto = new UpdateLotDto(
                "Updated Title",
                null, null, null, null
        );

        // Act & Assert
        assertThatThrownBy(() -> lotService.updateLot(created.id(), updateDto, otherUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only owner can update lot");
    }
}