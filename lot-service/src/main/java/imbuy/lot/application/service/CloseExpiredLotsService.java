package imbuy.lot.application.service;

import imbuy.lot.application.port.in.CloseExpiredLotsUseCase;
import imbuy.lot.application.port.out.BidPort;
import imbuy.lot.application.port.out.LotRepositoryPort;
import imbuy.lot.domain.model.Lot;
import imbuy.lot.domain.service.LotDomainService;
import imbuy.lot.domain.enums.LotStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CloseExpiredLotsService implements CloseExpiredLotsUseCase {

    private final LotRepositoryPort lotRepository;
    private final BidPort bidPort;
    private final LotDomainService domainService;

    @Override
    public void closeExpiredLots() {
        int page = 0;
        int size = 100;

        while (true) {
            var lots = lotRepository.findByStatus(
                    LotStatus.ACTIVE,
                    PageRequest.of(page, size)
            );

            if (lots.isEmpty()) break;

            for (Lot lot : lots) {
                if (lot.getEndDate() != null &&
                        lot.getEndDate().isBefore(LocalDateTime.now())) {

                    Long winnerId = bidPort.getAuctionWinner(lot.getId());
                    Lot closed = domainService.close(lot, winnerId);
                    lotRepository.save(closed);
                }
            }

            page++;
        }
    }
}
