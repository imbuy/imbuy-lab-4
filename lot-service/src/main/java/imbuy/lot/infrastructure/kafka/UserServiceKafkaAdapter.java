package imbuy.lot.infrastructure.kafka;

import com.imbuy.events.TopicNames;
import com.imbuy.events.user.UserRequestEvent;
import com.imbuy.events.user.UserResponseEvent;
import imbuy.lot.application.dto.UserDto;
import imbuy.lot.application.port.out.UserPort;
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
public class UserServiceKafkaAdapter implements UserPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final Map<String, CompletableFuture<UserDto>> pendingRequests = new ConcurrentHashMap<>();

    @Override
    public UserDto getUserById(Long userId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("user-service");

        return circuitBreaker.run(
                () -> {
                    String requestId = UUID.randomUUID().toString();
                    CompletableFuture<UserDto> future = new CompletableFuture<>();
                    pendingRequests.put(requestId, future);

                    try {
                        UserRequestEvent request = new UserRequestEvent(
                                "lot-service",
                                userId,
                                requestId,
                                "GET_USER_BY_ID"
                        );

                        log.info("Sending user request via Kafka: requestId={}, userId={}", requestId, userId);
                        kafkaTemplate.send(TopicNames.USER_REQUESTS, request);

                        UserDto result = future.get(5, TimeUnit.SECONDS);
                        return result;
                    } catch (Exception e) {
                        log.error("Error getting user via Kafka: {}", e.getMessage(), e);
                        pendingRequests.remove(requestId);
                        throw new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "User service is unavailable",
                                e
                        );
                    } finally {
                        pendingRequests.remove(requestId);
                    }
                },
                throwable -> {
                    log.error("Circuit breaker opened for user service: {}", throwable.getMessage());
                    throw new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "User service is temporarily unavailable. Please try again later."
                    );
                }
        );
    }

    @KafkaListener(topics = TopicNames.USER_RESPONSES, groupId = "lot-service")
    public void handleUserResponse(UserResponseEvent response, Acknowledgment acknowledgment) {
        try {
            log.info("Received user response: requestId={}, success={}",
                    response.getRequestId(), response.getSuccess());

            CompletableFuture<UserDto> future = pendingRequests.remove(response.getRequestId());
            if (future != null) {
                if (Boolean.TRUE.equals(response.getSuccess())) {
                    UserDto userDto = new UserDto(
                            response.getUserId(),
                            response.getUsername(),
                            response.getEmail(),
                            response.getRole()
                    );
                    future.complete(userDto);
                } else {
                    future.completeExceptionally(
                            new RuntimeException(response.getErrorMessage() != null ?
                                    response.getErrorMessage() : "User not found")
                    );
                }
            } else {
                log.warn("No pending request found for requestId: {}", response.getRequestId());
            }
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error handling user response: {}", e.getMessage(), e);
        }
    }
}