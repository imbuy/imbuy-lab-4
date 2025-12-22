package imbuy.lot.events;
import com.imbuy.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LotCreatedEvent extends BaseEvent {
    private Long lotId;
    private String title;
    private Long ownerId;
    private Long categoryId;
    private BigDecimal startPrice;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
