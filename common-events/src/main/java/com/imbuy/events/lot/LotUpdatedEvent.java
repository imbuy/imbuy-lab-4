package com.imbuy.events.lot;

import com.imbuy.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LotUpdatedEvent extends BaseEvent {
    private Long lotId;
    private String title;
    private Long ownerId;

    public LotUpdatedEvent(String sourceService, Long lotId, String title, Long ownerId) {
        super(sourceService);
        this.lotId = lotId;
        this.title = title;
        this.ownerId = ownerId;
    }
}

