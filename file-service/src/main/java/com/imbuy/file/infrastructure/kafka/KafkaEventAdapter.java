package com.imbuy.file.infrastructure.kafka;

import com.imbuy.events.BaseEvent;
import com.imbuy.file.application.port.out.KafkaEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventAdapter implements KafkaEventPort {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @Override
    public void publishEvent(String topic, BaseEvent event) {
        log.info("Publishing event to topic {}: {}", topic, event);
        kafkaTemplate.send(topic, event);
    }
}

