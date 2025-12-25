package com.imbuy.notification.infrastructure.kafka;

import com.imbuy.events.BaseEvent;
import com.imbuy.events.TopicNames;
import com.imbuy.events.bid.BidPlacedEvent;
import com.imbuy.events.lot.LotCreatedEvent;
import com.imbuy.events.lot.LotStatusChangedEvent;
import com.imbuy.events.notification.NotificationEvent;
import com.imbuy.notification.application.port.in.SendNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final SendNotificationUseCase sendNotificationUseCase;

    @KafkaListener(topics = TopicNames.NOTIFICATIONS, groupId = "notification-service")
    public void handleNotificationEvent(@Payload NotificationEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received notification event: {}", event);
            sendNotificationUseCase.sendNotification(
                    event.getUserId(),
                    event.getType(),
                    event.getTitle(),
                    event.getMessage()
            );
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = TopicNames.LOT_EVENTS, groupId = "notification-service")
    public void handleLotEvents(@Payload BaseEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received lot event: {}", event);
            
            if (event instanceof LotCreatedEvent lotCreated) {
                sendNotificationUseCase.sendNotification(
                        lotCreated.getOwnerId(),
                        "WEBSOCKET",
                        "New Lot Created",
                        String.format("Your lot '%s' has been created successfully", lotCreated.getTitle())
                );
            } else if (event instanceof LotStatusChangedEvent statusChanged) {
                String message = switch (statusChanged.getNewStatus()) {
                    case "ACTIVE" -> String.format("Lot #%d is now active", statusChanged.getLotId());
                    case "COMPLETED" -> String.format("Lot #%d has been completed. Winner: user #%d", 
                            statusChanged.getLotId(), statusChanged.getWinnerId());
                    default -> String.format("Lot #%d status changed to %s", 
                            statusChanged.getLotId(), statusChanged.getNewStatus());
                };
                sendNotificationUseCase.sendNotification(
                        statusChanged.getLotId(),
                        "WEBSOCKET",
                        "Lot Status Changed",
                        message
                );
            }
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing lot event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = TopicNames.BID_EVENTS, groupId = "notification-service")
    public void handleBidEvents(@Payload BaseEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received bid event: {}", event);
            
            if (event instanceof BidPlacedEvent bidPlaced) {
                sendNotificationUseCase.sendNotification(
                        bidPlaced.getBidderId(),
                        "WEBSOCKET",
                        "Bid Placed",
                        String.format("Your bid of %.2f has been placed on lot #%d", 
                                bidPlaced.getAmount(), bidPlaced.getLotId())
                );
            }
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing bid event: {}", e.getMessage(), e);
        }
    }
}

