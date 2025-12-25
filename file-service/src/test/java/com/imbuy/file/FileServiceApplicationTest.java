package com.imbuy.file;

import com.imbuy.file.application.dto.FileDto;
import com.imbuy.file.application.port.in.GetFileUseCase;
import com.imbuy.file.application.port.in.UploadFileUseCase;
import com.imbuy.file.domain.model.FileMetadata;
import com.imbuy.file.infrastructure.persistence.JpaFileRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import io.jsonwebtoken.io.Decoders;
import java.nio.file.Path;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.import=optional:file:./config/",
                "spring.cloud.config.enabled=false"
        }
)
@Testcontainers
@AutoConfigureMockMvc
class FileServiceApplicationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.group-id", () -> "file-service-test");

        registry.add("file.storage.path", tempDir::toString);

        registry.add("app.security.jwt.secret", () -> "dGhpc19pcy1hLWxvbmdlci1iYXNlNjQtand0LXNlY3JldC1rZXk=");

        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("spring.cloud.config.import-check.enabled", () -> "false");

        registry.add("spring.cloud.discovery.enabled", () -> "false");
        registry.add("spring.cloud.service-registry.auto-registration.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UploadFileUseCase uploadFileUseCase;

    @Autowired
    private GetFileUseCase getFileUseCase;

    @Autowired
    private JpaFileRepository fileRepository;

    private String validToken;

    @BeforeAll
    static void beforeAll() {
        System.out.println("PostgreSQL URL: " + postgres.getJdbcUrl());
        System.out.println("Kafka Bootstrap Servers: " + kafka.getBootstrapServers());
        System.out.println("Temp directory: " + tempDir.toString());
    }

    @BeforeEach
    void setUp() {
        fileRepository.deleteAll();

        validToken = generateJwtToken("testuser", 1L);
    }

    private String generateJwtToken(String username, Long userId) {
        byte[] keyBytes = Decoders.BASE64.decode("dGhpc19pcy1hLWxvbmdlci1iYXNlNjQtand0LXNlY3JldC1rZXk=");
        Key key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
    }

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
        assertThat(uploadFileUseCase).isNotNull();
        assertThat(getFileUseCase).isNotNull();
        assertThat(fileRepository).isNotNull();
    }

    @Test
    void shouldUploadFileWithLotId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/files/upload")
                        .file(file)
                        .param("lotId", "123")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.contentType").value(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(jsonPath("$.fileSize").value(18));

        Optional<FileMetadata> savedFile = fileRepository.findAll().stream()
                .filter(f -> f.getFileName().equals("test-image.jpg"))
                .findFirst();
        assertThat(savedFile).isPresent();
        assertThat(savedFile.get().getLotId()).isEqualTo(123L);
    }

    @Test
    void shouldNotUploadFileWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/files/upload")
                        .file(file)
                        .param("lotId", "123")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetFileMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test pdf content".getBytes()
        );

        FileDto uploadedFile = uploadFileUseCase.uploadFile(file, 456L);

        mockMvc.perform(get("/files/{id}", uploadedFile.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(uploadedFile.getId()))
                .andExpect(jsonPath("$.fileName").value("test-document.pdf"))
                .andExpect(jsonPath("$.contentType").value(MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    void shouldDownloadFile() throws Exception {
        String fileContent = "Hello, World! Test download content.";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                fileContent.getBytes()
        );

        FileDto uploadedFile = uploadFileUseCase.uploadFile(file, 789L);

        mockMvc.perform(get("/files/{id}/download", uploadedFile.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"" + uploadedFile.getFileName() + "\""))
                .andExpect(content().bytes(fileContent.getBytes()));
    }

    @Test
    void shouldNotAccessFileWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        FileDto uploadedFile = uploadFileUseCase.uploadFile(file, 999L);

        mockMvc.perform(get("/files/{id}", uploadedFile.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSaveLotIdInDatabase() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test".getBytes()
        );

        Long lotId = 555L;

        FileDto result = uploadFileUseCase.uploadFile(file, lotId);

        Optional<FileMetadata> saved = fileRepository.findById(result.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getLotId()).isEqualTo(lotId);
        assertThat(saved.get().getFileName()).isEqualTo("test.jpg");
    }

    @Test
    void shouldGetFileResource() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-resource.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Resource content".getBytes()
        );

        FileDto uploaded = uploadFileUseCase.uploadFile(file, 777L);
        Resource resource = getFileUseCase.getFileResource(uploaded.getId());

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void shouldReturnNotFoundForNonExistentFile() throws Exception {
        Long nonExistentId = 999999L;

        mockMvc.perform(get("/files/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUploadMultipleFiles() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "file1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Content 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "file2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Content 2".getBytes()
        );

        mockMvc.perform(multipart("/files/upload")
                        .file(file1)
                        .param("lotId", "100")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        mockMvc.perform(multipart("/files/upload")
                        .file(file2)
                        .param("lotId", "200")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        assertThat(fileRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldHandleLargeFileUpload() throws Exception {
        byte[] largeContent = new byte[1024 * 1024];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-file.bin",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                largeContent
        );

        mockMvc.perform(multipart("/files/upload")
                        .file(largeFile)
                        .param("lotId", "300")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileSize").value(largeContent.length));
    }
}