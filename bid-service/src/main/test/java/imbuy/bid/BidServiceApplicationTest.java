package imbuy.bid;

import imbuy.bid.application.dto.BidDto;
import imbuy.bid.application.dto.CreateBidDto;
import imbuy.bid.application.mapper.BidMapper;
import imbuy.bid.application.port.out.BidRepositoryPort;
import imbuy.bid.application.service.BidService;
import imbuy.bid.domain.model.Bid;
import imbuy.bid.infrastructure.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.kafka.bootstrap-servers=dummy:1234"
})
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BidServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("bid_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", () -> "dummy:1234");
        registry.add("app.security.jwt.secret", () -> "test_jwt_secret_for_tests");
    }

    @Autowired
    private BidService bidService;

    @Autowired
    private BidRepositoryPort bidRepository;

    @Autowired
    private BidMapper bidMapper;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final Long testLotId = 1L;
    private Bid testBid;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;


    @BeforeEach
    void setUp() {
        when(kafkaTemplate.send(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(null);

        when(jwtService.isTokenValid(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);
        when(jwtService.extractUserId(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(1L);
        when(jwtService.extractUsername(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("User 1");

        bidRepository.deleteAll().block();

        testBid = Bid.builder()
                .lotId(testLotId)
                .bidderId(1L)
                .amount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();

        testBid = bidRepository.save(testBid).block();
    }

    @Test
    void placeBid_shouldCreateNewBid() {
        CreateBidDto request = new CreateBidDto(new BigDecimal("110.00"));

        BidDto result = bidService.placeBid(testLotId, request, 2L).block();

        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualTo(new BigDecimal("110.00"));
        assertThat(result.bidder_id()).isEqualTo(2L);
        assertThat(result.bidder_username()).isEqualTo("User 2");
    }

    @Test
    void getAuctionWinnerId_shouldReturnHighestBidder() {
        Bid higherBid = Bid.builder()
                .lotId(testLotId)
                .bidderId(2L)
                .amount(new BigDecimal("200.00"))
                .createdAt(LocalDateTime.now().plusMinutes(1))
                .build();
        bidRepository.save(higherBid).block();

        Long winnerId = bidService.getAuctionWinnerId(testLotId).block();

        assertThat(winnerId).isEqualTo(2L);
    }

    @Test
    void getBidsByLotId_shouldReturnPaginated() {
        Bid anotherBid = Bid.builder()
                .lotId(testLotId)
                .bidderId(2L)
                .amount(new BigDecimal("150.00"))
                .createdAt(LocalDateTime.now().plusMinutes(1))
                .build();
        bidRepository.save(anotherBid).block();

        List<BidDto> result = bidService.getBidsByLotId(testLotId, PageRequest.of(0, 10))
                .collectList().block();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).amount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(result.get(1).amount()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void bidMapper_mapToDto_shouldMapCorrectly() {
        BidDto dto = bidMapper.mapToDto(testBid);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(testBid.getId());
        assertThat(dto.amount()).isEqualTo(testBid.getAmount());
        assertThat(dto.bidder_id()).isEqualTo(testBid.getBidderId());
        assertThat(dto.bidder_username()).isEqualTo("User " + testBid.getBidderId());
    }
}
