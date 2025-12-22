package imbuy.lot.domain.service;

import imbuy.lot.domain.model.Lot;
import imbuy.lot.domain.enums.LotStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Component
public class LotDomainService {

    public void validateBidStep(BigDecimal bidStep) {
        if (bidStep == null || bidStep.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bid step must be greater than 0");
        }
    }

    public void validateEndDate(LocalDateTime endDate) {
        if (endDate != null && endDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date cannot be in the past");
        }
    }

    public Lot approve(Lot lot) {
        if (lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Lot is not pending approval");
        }
        return lot.toBuilder()
                .status(LotStatus.ACTIVE)
                .build();
    }

    public Lot cancel(Lot lot) {
        if (lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Lot cannot be cancelled");
        }
        return lot.toBuilder()
                .status(LotStatus.CANCELLED)
                .build();
    }
    public Lot close(Lot lot, Long winnerId) {
        if (lot.getStatus() != LotStatus.ACTIVE) {
            throw new IllegalStateException("Only active lots can be closed");
        }

        return lot.toBuilder()
                .status(LotStatus.COMPLETED)
                .winnerId(winnerId)
                .build();
    }

}
