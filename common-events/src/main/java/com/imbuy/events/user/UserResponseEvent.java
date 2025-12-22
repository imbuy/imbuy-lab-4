package com.imbuy.events.user;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserResponseEvent extends BaseEvent {
    private String requestId;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private Boolean success;
    private String errorMessage;

    public UserResponseEvent(String sourceService, String requestId, Long userId, String username, 
                             String email, String role, Boolean success, String errorMessage) {
        super(sourceService);
        this.requestId = requestId;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.success = success;
        this.errorMessage = errorMessage;
    }
}

