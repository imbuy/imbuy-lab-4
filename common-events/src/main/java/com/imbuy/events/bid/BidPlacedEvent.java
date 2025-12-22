package com.imbuy.events.bid;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BidPlacedEvent extends BaseEvent {
    private Long bidId;
    private Long lotId;
    private Long bidderId;
    private BigDecimal amount;

    public BidPlacedEvent(String sourceService, Long bidId, Long lotId, Long bidderId, BigDecimal amount) {
        super(sourceService);
        this.bidId = bidId;
        this.lotId = lotId;
        this.bidderId = bidderId;
        this.amount = amount;
    }
}

