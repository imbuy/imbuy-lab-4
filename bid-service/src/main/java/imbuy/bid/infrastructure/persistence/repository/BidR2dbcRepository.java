package imbuy.bid.infrastructure.persistence.repository;

import imbuy.bid.infrastructure.persistence.entity.BidEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface BidR2dbcRepository
        extends ReactiveCrudRepository<BidEntity, Long> {

    Flux<BidEntity> findByLotIdOrderByCreatedAtDesc(Long lotId, Pageable pageable);

    Mono<BidEntity> findHighestBidByLotId(Long lotId);

    Mono<BigDecimal> findMaxBidAmountByLotId(Long lotId);

    Mono<Long> countByLotId(Long lotId);
}
