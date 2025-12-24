package imbuy.user.auth;

import imbuy.user.application.dto.*;
import imbuy.user.domain.model.Role;
import imbuy.user.infrastructure.security.JwtServiceAdapter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
                    .asCompatibleSubstituteFor("apache/kafka")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");

        registry.add("app.security.jwt.secret", () -> "dGhpc19pcy1hLWxvbmdlci1iYXNlNjQtand0LXNlY3JldC1rZXk=");
        registry.add("app.security.jwt.access-token-validity", () -> "3600000");
        registry.add("app.security.jwt.refresh-token-validity", () -> "1209600000");
        registry.add("app.security.default-supervisor.email", () -> "supervisor@test.com");
        registry.add("app.security.default-supervisor.password", () -> "Supervisor123!");
        registry.add("app.security.default-supervisor.username", () -> "TestSupervisor");
    }

    @Autowired
    private imbuy.user.application.port.in.AuthUseCase authUseCase;

    @Autowired
    private imbuy.user.application.port.in.UserUseCase userUseCase;

    @Autowired
    private JwtServiceAdapter jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static String testUserEmail = "test@example.com";
    private static String testUserPassword = "Password123!";
    private static Long testUserId;

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

    @BeforeEach
    void setUp() {
        when(kafkaTemplate.send(any(), any())).thenReturn(null);
    }

    @Test
    @Order(1)
    void shouldRegisterUser() {
        RegisterRequest registerRequest = new RegisterRequest(
                testUserEmail,
                testUserPassword,
                "testuser",
                Role.USER
        );

        StepVerifier.create(authUseCase.register(registerRequest))
                .assertNext(userDto -> {
                    assertThat(userDto).isNotNull();
                    assertThat(userDto.email()).isEqualTo(testUserEmail);
                    assertThat(userDto.username()).isEqualTo("testuser");
                    assertThat(userDto.role()).isEqualTo(Role.USER);
                    testUserId = userDto.id();
                })
                .verifyComplete();
    }

    @Test
    @Order(2)
    void shouldFailRegisterWithExistingEmail() {
        RegisterRequest registerRequest = new RegisterRequest(
                testUserEmail,
                "AnotherPassword123!",
                "anotheruser",
                Role.USER
        );

        StepVerifier.create(authUseCase.register(registerRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Email exists"))
                .verify();
    }

    @Test
    @Order(3)
    void shouldLoginWithValidCredentials() {
        LoginRequest loginRequest = new LoginRequest(
                testUserEmail,
                testUserPassword
        );

        StepVerifier.create(authUseCase.login(loginRequest))
                .assertNext(authResponse -> {
                    assertThat(authResponse).isNotNull();
                    assertThat(authResponse.user().email()).isEqualTo(testUserEmail);
                    assertThat(authResponse.accessToken()).isNotBlank();
                    assertThat(authResponse.refreshToken()).isNotBlank();

                    assertThat(jwtService.isValid(authResponse.accessToken(), testUserEmail)).isTrue();
                    assertThat(jwtService.isRefresh(authResponse.refreshToken())).isTrue();
                })
                .verifyComplete();
    }

    @Test
    @Order(4)
    void shouldFailLoginWithWrongPassword() {
        LoginRequest loginRequest = new LoginRequest(
                testUserEmail,
                "WrongPassword123!"
        );

        StepVerifier.create(authUseCase.login(loginRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Invalid credentials"))
                .verify();
    }

    @Test
    @Order(5)
    void shouldFailLoginWithNonExistentEmail() {
        LoginRequest loginRequest = new LoginRequest(
                "nonexistent@example.com",
                "Password123!"
        );

        StepVerifier.create(authUseCase.login(loginRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Invalid credentials"))
                .verify();
    }

    @Test
    @Order(8)
    void shouldFailRefreshWithAccessToken() {
        LoginRequest loginRequest = new LoginRequest(testUserEmail, testUserPassword);
        AuthResponse authResponse = authUseCase.login(loginRequest).block(Duration.ofSeconds(5));

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(
                authResponse.accessToken()
        );

        StepVerifier.create(authUseCase.refresh(refreshRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Invalid refresh token"))
                .verify();
    }

    @Test
    @Order(15)
    void shouldCreateSupervisorUser() {
        RegisterRequest supervisorRequest = new RegisterRequest(
                "supervisor2@example.com",
                "Supervisor123!",
                "supervisor2",
                Role.SUPERVISOR
        );

        StepVerifier.create(authUseCase.register(supervisorRequest))
                .assertNext(userDto -> {
                    assertThat(userDto).isNotNull();
                    assertThat(userDto.email()).isEqualTo("supervisor2@example.com");
                    assertThat(userDto.role()).isEqualTo(Role.SUPERVISOR);
                    assertThat(userDto.username()).isEqualTo("supervisor2");
                })
                .verifyComplete();
    }

    @Test
    @Order(18)
    void shouldRegisterUserWithNullUsername() {
        RegisterRequest request = new RegisterRequest(
                "noname@example.com",
                "Password123!",
                null,
                Role.USER
        );

        StepVerifier.create(authUseCase.register(request))
                .assertNext(userDto -> {
                    assertThat(userDto.email()).isEqualTo("noname@example.com");
                    assertThat(userDto.username()).isNull();
                    assertThat(userDto.role()).isEqualTo(Role.USER);
                })
                .verifyComplete();
    }

    @Test
    @Order(19)
    void shouldRegisterUserWithDefaultRole() {
        RegisterRequest request = new RegisterRequest(
                "defaultrole@example.com",
                "Password123!",
                "defaultuser",
                null
        );

        StepVerifier.create(authUseCase.register(request))
                .assertNext(userDto -> {
                    assertThat(userDto.email()).isEqualTo("defaultrole@example.com");
                    assertThat(userDto.role()).isEqualTo(Role.USER);
                })
                .verifyComplete();
    }

    @Test
    @Order(22)
    void shouldValidatePasswordEncoding() {
        String rawPassword = "TestPassword123!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).isNotBlank();
        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
        assertThat(passwordEncoder.matches("WrongPassword", encoded)).isFalse();
    }

    @Test
    @Order(23)
    void testUserDomainModel() {
        imbuy.user.domain.model.User user = imbuy.user.domain.model.User.builder()
                .id(100L)
                .email("domain@test.com")
                .password("encodedPassword")
                .username("domainuser")
                .role(Role.SUPERVISOR)
                .active(true)
                .build();

        assertThat(user.getId()).isEqualTo(100L);
        assertThat(user.getEmail()).isEqualTo("domain@test.com");
        assertThat(user.getUsername()).isEqualTo("domainuser");
        assertThat(user.getRole()).isEqualTo(Role.SUPERVISOR);
        assertThat(user.isSupervisor()).isTrue();

        imbuy.user.domain.model.User updatedUser = user.toBuilder()
                .username("updatedname")
                .build();

        assertThat(updatedUser.getId()).isEqualTo(user.getId());
        assertThat(updatedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(updatedUser.getUsername()).isEqualTo("updatedname");
    }

    @Test
    @Order(24)
    void shouldCleanUpTestData() {
        assertThat(testUserId).isNotNull();
        assertThat(testUserEmail).isNotBlank();

        System.out.println("Тесты завершены. Test user ID: " + testUserId);
    }
}