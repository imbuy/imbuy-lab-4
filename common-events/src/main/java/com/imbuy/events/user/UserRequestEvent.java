package com.imbuy.events.user;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRequestEvent extends BaseEvent {
    private Long userId;
    private String requestId;
    private String requestType; // GET_USER_BY_ID, VALIDATE_USER, etc.

    public UserRequestEvent(String sourceService, Long userId, String requestId, String requestType) {
        super(sourceService);
        this.userId = userId;
        this.requestId = requestId;
        this.requestType = requestType;
    }
}

