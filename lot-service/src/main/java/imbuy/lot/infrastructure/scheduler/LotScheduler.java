package imbuy.lot.infrastructure.scheduler;

import imbuy.lot.application.port.in.CloseExpiredLotsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LotScheduler {

    private final CloseExpiredLotsUseCase useCase;

    @Scheduled(fixedRate = 60000)
    public void run() {
        log.info("Starting lot expiration job");
        useCase.closeExpiredLots();
    }
}
