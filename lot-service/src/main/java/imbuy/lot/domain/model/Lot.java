package imbuy.lot.domain.model;

import imbuy.lot.domain.enums.LotStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Lot {
    private Long id;
    private String title;
    private String description;
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal bidStep;
    private Long ownerId;
    private Long categoryId;
    private Long winnerId;
    private LotStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}
