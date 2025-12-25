package imbuy.bid.application.port.out;

import imbuy.bid.domain.model.Bid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface BidRepositoryPort {

    Flux<Bid> findByLotId(Long lotId, org.springframework.data.domain.Pageable pageable);

    Mono<Bid> save(Bid bid);

    Mono<BigDecimal> findMaxBidAmountByLotId(Long lotId);

    Mono<Long> countBidsForLot(Long lotId);

    Mono<Bid> findHighestBidByLotId(Long lotId);

    Mono<Void> deleteAll();
}
