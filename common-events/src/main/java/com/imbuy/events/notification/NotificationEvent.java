package com.imbuy.events.notification;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends BaseEvent {
    private Long userId;
    private String type; // EMAIL, SMS, PUSH, WEBSOCKET
    private String title;
    private String message;
    private Map<String, String> metadata;

    public NotificationEvent(String sourceService, Long userId, String type, 
                            String title, String message, Map<String, String> metadata) {
        super(sourceService);
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.metadata = metadata;
    }
}

