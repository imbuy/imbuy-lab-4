package com.imbuy.events.lot;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LotCreatedEvent extends BaseEvent {
    private Long lotId;
    private String title;
    private Long ownerId;
    private BigDecimal startPrice;
    private LocalDateTime endDate;

    public LotCreatedEvent(String sourceService, Long lotId, String title, Long ownerId, 
                          BigDecimal startPrice, LocalDateTime endDate) {
        super(sourceService);
        this.lotId = lotId;
        this.title = title;
        this.ownerId = ownerId;
        this.startPrice = startPrice;
        this.endDate = endDate;
    }
}

