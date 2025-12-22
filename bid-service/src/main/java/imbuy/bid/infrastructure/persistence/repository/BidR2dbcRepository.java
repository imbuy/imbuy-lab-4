package imbuy.bid.infrastructure.persistence.repository;

import imbuy.bid.infrastructure.persistence.entity.BidEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Repository
public interface BidR2dbcRepository
        extends ReactiveCrudRepository<BidEntity, Long> {

    Flux<BidEntity> findByLotIdOrderByCreatedAtDesc(Long lotId, Pageable pageable);

    @Query("""
        SELECT *
        FROM bids
        WHERE lot_id = :lotId
        ORDER BY amount DESC, created_at ASC
        LIMIT 1
    """)
    Mono<BidEntity> findHighestBidByLotId(Long lotId);

    Mono<BigDecimal> findMaxBidAmountByLotId(Long lotId);

    Mono<Long> countByLotId(Long lotId);
}
