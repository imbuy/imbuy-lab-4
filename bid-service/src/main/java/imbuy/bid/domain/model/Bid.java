package imbuy.bid.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

    private Long id;
    private Long lotId;
    private Long bidderId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
