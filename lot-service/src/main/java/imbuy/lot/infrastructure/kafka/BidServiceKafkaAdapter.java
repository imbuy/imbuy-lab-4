package imbuy.lot.infrastructure.kafka;

import com.imbuy.events.TopicNames;
import com.imbuy.events.bid.BidWinnerRequestEvent;
import com.imbuy.events.bid.BidWinnerResponseEvent;
import imbuy.lot.application.port.out.BidPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidServiceKafkaAdapter implements BidPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final Map<String, CompletableFuture<Long>> pendingRequests = new ConcurrentHashMap<>();

    @Override
    public Long getAuctionWinner(Long lotId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("bid-service");

        return circuitBreaker.run(
                () -> {
                    String requestId = UUID.randomUUID().toString();
                    CompletableFuture<Long> future = new CompletableFuture<>();
                    pendingRequests.put(requestId, future);

                    try {
                        BidWinnerRequestEvent request = new BidWinnerRequestEvent(
                                "lot-service",
                                lotId,
                                requestId
                        );

                        log.info("Sending bid winner request via Kafka: requestId={}, lotId={}", requestId, lotId);
                        kafkaTemplate.send(TopicNames.BID_REQUESTS, request);

                        Long result = future.get(5, TimeUnit.SECONDS);
                        return result;
                    } catch (Exception e) {
                        log.error("Error getting auction winner via Kafka: {}", e.getMessage(), e);
                        pendingRequests.remove(requestId);
                        throw new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Bid service is unavailable",
                                e
                        );
                    } finally {
                        pendingRequests.remove(requestId);
                    }
                },
                throwable -> {
                    log.error("Circuit breaker opened for bid service: {}", throwable.getMessage());
                    throw new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "Bid service is temporarily unavailable. Please try again later."
                    );
                }
        );
    }

    @KafkaListener(topics = TopicNames.BID_RESPONSES, groupId = "lot-service")
    public void handleBidWinnerResponse(BidWinnerResponseEvent response, Acknowledgment acknowledgment) {
        try {
            log.info("Received bid winner response: requestId={}, success={}, winnerId={}",
                    response.getRequestId(), response.getSuccess(), response.getWinnerId());

            CompletableFuture<Long> future = pendingRequests.remove(response.getRequestId());
            if (future != null) {
                if (Boolean.TRUE.equals(response.getSuccess())) {
                    future.complete(response.getWinnerId());
                } else {
                    future.completeExceptionally(
                            new RuntimeException(response.getErrorMessage() != null ?
                                    response.getErrorMessage() : "Failed to get winner")
                    );
                }
            } else {
                log.warn("No pending request found for requestId: {}", response.getRequestId());
            }
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error handling bid winner response: {}", e.getMessage(), e);
        }
    }
}