package com.imbuy.file.application.port.out;

import com.imbuy.events.BaseEvent;

public interface KafkaEventPort {
    void publishEvent(String topic, BaseEvent event);
}

