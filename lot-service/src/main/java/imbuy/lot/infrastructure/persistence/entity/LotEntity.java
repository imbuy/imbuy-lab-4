package imbuy.lot.infrastructure.persistence.entity;

import imbuy.lot.domain.enums.LotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(name = "start_price")
    private BigDecimal startPrice;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "bid_step")
    private BigDecimal bidStep;

    private Long ownerId;
    private Long categoryId;
    private Long winnerId;

    @Enumerated(EnumType.STRING)
    private LotStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}
