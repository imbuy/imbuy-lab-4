package imbuy.bid.infrastructure.kafka;

import com.imbuy.events.TopicNames;
import com.imbuy.events.bid.BidWinnerRequestEvent;
import com.imbuy.events.bid.BidWinnerResponseEvent;
import imbuy.bid.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidRequestHandler {

    private final BidService bidService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = TopicNames.BID_REQUESTS, groupId = "bid-service")
    public void handleBidWinnerRequest(BidWinnerRequestEvent request, Acknowledgment acknowledgment) {
        log.info("Received bid winner request: requestId={}, lotId={}", 
                request.getRequestId(), request.getLotId());

        bidService.getAuctionWinnerId(request.getLotId())
                .subscribe(
                        winnerId -> {
                            BidWinnerResponseEvent response = new BidWinnerResponseEvent(
                                    "bid-service",
                                    request.getRequestId(),
                                    request.getLotId(),
                                    winnerId != null && winnerId > 0 ? winnerId : null,
                                    true,
                                    null
                            );
                            kafkaTemplate.send(TopicNames.BID_RESPONSES, response);
                            acknowledgment.acknowledge();
                            log.info("Sent bid winner response: requestId={}, winnerId={}", 
                                    request.getRequestId(), winnerId);
                        },
                        error -> {
                            log.error("Error processing bid winner request: {}", error.getMessage(), error);
                            BidWinnerResponseEvent response = new BidWinnerResponseEvent(
                                    "bid-service",
                                    request.getRequestId(),
                                    request.getLotId(),
                                    null,
                                    false,
                                    error.getMessage()
                            );
                            kafkaTemplate.send(TopicNames.BID_RESPONSES, response);
                            acknowledgment.acknowledge();
                        }
                );
    }
}

