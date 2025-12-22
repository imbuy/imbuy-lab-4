package com.imbuy.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.imbuy.events.bid.BidPlacedEvent;
import com.imbuy.events.bid.BidWinnerRequestEvent;
import com.imbuy.events.bid.BidWinnerResponseEvent;
import com.imbuy.events.file.FileDeletedEvent;
import com.imbuy.events.file.FileUploadedEvent;
import com.imbuy.events.lot.LotCreatedEvent;
import com.imbuy.events.lot.LotStatusChangedEvent;
import com.imbuy.events.lot.LotUpdatedEvent;
import com.imbuy.events.notification.NotificationEvent;
import com.imbuy.events.user.UserCreatedEvent;
import com.imbuy.events.user.UserRequestEvent;
import com.imbuy.events.user.UserResponseEvent;
import com.imbuy.events.user.UserUpdatedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UserCreatedEvent.class, name = "USER_CREATED"),
    @JsonSubTypes.Type(value = UserUpdatedEvent.class, name = "USER_UPDATED"),
    @JsonSubTypes.Type(value = UserRequestEvent.class, name = "USER_REQUEST"),
    @JsonSubTypes.Type(value = UserResponseEvent.class, name = "USER_RESPONSE"),
    @JsonSubTypes.Type(value = LotCreatedEvent.class, name = "LOT_CREATED"),
    @JsonSubTypes.Type(value = LotUpdatedEvent.class, name = "LOT_UPDATED"),
    @JsonSubTypes.Type(value = LotStatusChangedEvent.class, name = "LOT_STATUS_CHANGED"),
    @JsonSubTypes.Type(value = BidPlacedEvent.class, name = "BID_PLACED"),
    @JsonSubTypes.Type(value = BidWinnerRequestEvent.class, name = "BID_WINNER_REQUEST"),
    @JsonSubTypes.Type(value = BidWinnerResponseEvent.class, name = "BID_WINNER_RESPONSE"),
    @JsonSubTypes.Type(value = NotificationEvent.class, name = "NOTIFICATION"),
    @JsonSubTypes.Type(value = FileUploadedEvent.class, name = "FILE_UPLOADED"),
    @JsonSubTypes.Type(value = FileDeletedEvent.class, name = "FILE_DELETED")
})
public abstract class BaseEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private String sourceService;

    public BaseEvent(String sourceService) {
        this.eventId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
        this.sourceService = sourceService;
    }
}

