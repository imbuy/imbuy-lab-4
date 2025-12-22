package com.imbuy.events.bid;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BidWinnerResponseEvent extends BaseEvent {
    private String requestId;
    private Long lotId;
    private Long winnerId;
    private Boolean success;
    private String errorMessage;

    public BidWinnerResponseEvent(String sourceService, String requestId, Long lotId, 
                                  Long winnerId, Boolean success, String errorMessage) {
        super(sourceService);
        this.requestId = requestId;
        this.lotId = lotId;
        this.winnerId = winnerId;
        this.success = success;
        this.errorMessage = errorMessage;
    }
}

