package com.imbuy.events.user;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserUpdatedEvent extends BaseEvent {
    private Long userId;
    private String username;
    private String email;

    public UserUpdatedEvent(String sourceService, Long userId, String username, String email) {
        super(sourceService);
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}

