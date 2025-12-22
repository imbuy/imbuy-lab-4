package imbuy.lot.infrastructure.kafka;

import com.imbuy.events.TopicNames;
import com.imbuy.events.bid.BidWinnerRequestEvent;
import com.imbuy.events.bid.BidWinnerResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidServiceKafkaAdapter {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, CompletableFuture<Long>> pendingRequests = new ConcurrentHashMap<>();

    public Long getAuctionWinner(Long lotId) {
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

            // Wait for response with timeout
            Long result = future.get(5, TimeUnit.SECONDS);
            return result;
        } catch (Exception e) {
            log.error("Error getting auction winner via Kafka: {}", e.getMessage(), e);
            pendingRequests.remove(requestId);
            throw new RuntimeException("Failed to get auction winner: " + e.getMessage(), e);
        } finally {
            pendingRequests.remove(requestId);
        }
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

