package com.imbuy.events.bid;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BidWinnerRequestEvent extends BaseEvent {
    private Long lotId;
    private String requestId;

    public BidWinnerRequestEvent(String sourceService, Long lotId, String requestId) {
        super(sourceService);
        this.lotId = lotId;
        this.requestId = requestId;
    }
}

