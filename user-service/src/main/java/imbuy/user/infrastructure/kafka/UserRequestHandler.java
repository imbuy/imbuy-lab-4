package imbuy.user.infrastructure.kafka;

import com.imbuy.events.TopicNames;
import com.imbuy.events.user.UserRequestEvent;
import com.imbuy.events.user.UserResponseEvent;
import imbuy.user.mapper.UserMapper;
import imbuy.user.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRequestHandler {

    private final AuthUserService authUserService;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = TopicNames.USER_REQUESTS, groupId = "user-service")
    public void handleUserRequest(UserRequestEvent request, Acknowledgment acknowledgment) {
        log.info("Received user request: requestId={}, userId={}, type={}", 
                request.getRequestId(), request.getUserId(), request.getRequestType());

        if ("GET_USER_BY_ID".equals(request.getRequestType())) {
            authUserService.findById(request.getUserId())
                    .map(user -> {
                        UserResponseEvent response = new UserResponseEvent(
                                "user-service",
                                request.getRequestId(),
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getRole() != null ? user.getRole().name() : null,
                                true,
                                null
                        );
                        kafkaTemplate.send(TopicNames.USER_RESPONSES, response);
                        acknowledgment.acknowledge();
                        log.info("Sent user response: requestId={}, userId={}", 
                                request.getRequestId(), user.getId());
                        return response;
                    })
                    .switchIfEmpty(Mono.fromRunnable(() -> {
                        UserResponseEvent response = new UserResponseEvent(
                                "user-service",
                                request.getRequestId(),
                                null,
                                null,
                                null,
                                null,
                                false,
                                "User not found"
                        );
                        kafkaTemplate.send(TopicNames.USER_RESPONSES, response);
                        acknowledgment.acknowledge();
                        log.info("Sent user not found response: requestId={}", request.getRequestId());
                    }))
                    .onErrorResume(error -> {
                        log.error("Error processing user request: {}", error.getMessage(), error);
                        UserResponseEvent response = new UserResponseEvent(
                                "user-service",
                                request.getRequestId(),
                                null,
                                null,
                                null,
                                null,
                                false,
                                error.getMessage()
                        );
                        kafkaTemplate.send(TopicNames.USER_RESPONSES, response);
                        acknowledgment.acknowledge();
                        return Mono.empty();
                    })
                    .subscribe();
        } else {
            acknowledgment.acknowledge();
        }
    }
}

