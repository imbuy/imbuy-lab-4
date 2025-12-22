package imbuy.bid.infrastructure.persistence.adapter;

import imbuy.bid.application.port.out.BidRepositoryPort;
import imbuy.bid.domain.model.Bid;
import imbuy.bid.infrastructure.persistence.mapper.BidPersistenceMapper;
import imbuy.bid.infrastructure.persistence.repository.BidR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BidRepositoryAdapter implements BidRepositoryPort {

    private final BidR2dbcRepository repository;
    private final BidPersistenceMapper mapper;

    @Override
    public Flux<Bid> findByLotId(Long lotId, Pageable pageable) {
        return repository.findByLotIdOrderByCreatedAtDesc(lotId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Bid> save(Bid bid) {
        return repository.save(mapper.toEntity(bid))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<BigDecimal> findMaxBidAmountByLotId(Long lotId) {
        return repository.findMaxBidAmountByLotId(lotId);
    }

    @Override
    public Mono<Long> countBidsForLot(Long lotId) {
        return repository.countByLotId(lotId);
    }

    @Override
    public Mono<Bid> findHighestBidByLotId(Long lotId) {
        return repository.findHighestBidByLotId(lotId)
                .map(mapper::toDomain);
    }
}
