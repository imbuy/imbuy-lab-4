package com.imbuy.events.lot;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LotStatusChangedEvent extends BaseEvent {
    private Long lotId;
    private String oldStatus;
    private String newStatus;
    private Long winnerId;

    public LotStatusChangedEvent(String sourceService, Long lotId, String oldStatus, 
                                String newStatus, Long winnerId) {
        super(sourceService);
        this.lotId = lotId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.winnerId = winnerId;
    }
}

