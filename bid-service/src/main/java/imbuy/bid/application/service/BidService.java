package imbuy.bid.application.service;

import com.imbuy.events.TopicNames;
import com.imbuy.events.bid.BidPlacedEvent;
import imbuy.bid.application.dto.BidDto;
import imbuy.bid.application.dto.CreateBidDto;
import imbuy.bid.application.mapper.BidMapper;
import imbuy.bid.application.port.in.BidUseCase;
import imbuy.bid.application.port.out.BidRepositoryPort;
import imbuy.bid.domain.model.Bid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BidService implements BidUseCase {

    private final BidRepositoryPort repository;
    private final BidMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    public Flux<BidDto> getBidsByLotId(Long lotId, Pageable pageable) {
        return repository.findByLotId(lotId, pageable)
                .map(mapper::mapToDto);
    }

    @Override
    public Mono<BidDto> placeBid(Long lotId, CreateBidDto dto, Long userId) {
        return validateBid(lotId, dto.amount(), userId)
                .then(createBid(lotId, dto.amount(), userId))
                .flatMap(repository::save)
                .flatMap(savedBid -> {
                    BidPlacedEvent event = new BidPlacedEvent(
                            "bid-service",
                            savedBid.getId(),
                            savedBid.getLotId(),
                            savedBid.getBidderId(),
                            savedBid.getAmount()
                    );

                    return Mono.fromRunnable(() ->
                            kafkaTemplate.send(TopicNames.BID_EVENTS, event)
                    ).thenReturn(savedBid);
                })
                .map(mapper::mapToDto);
    }


    private Mono<Void> validateBid(Long lotId, BigDecimal amount, Long bidderId) {
        return repository.findMaxBidAmountByLotId(lotId)
                .flatMap(maxBid -> {
                    if (maxBid != null) {
                        BigDecimal minBid = maxBid.add(new BigDecimal("10.00"));
                        if (amount.compareTo(minBid) < 0) {
                            return Mono.error(
                                    new ResponseStatusException(
                                            HttpStatus.BAD_REQUEST,
                                            String.format("Bid must be at least %.2f", minBid)
                                    )
                            );
                        }
                    }
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Bid> createBid(Long lotId, BigDecimal amount, Long bidderId) {
        return Mono.just(
                Bid.builder()
                        .lotId(lotId)
                        .bidderId(bidderId)
                        .amount(amount)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Override
    public Mono<Long> getAuctionWinnerId(Long lotId) {
        return repository.countBidsForLot(lotId)
                .flatMap(count -> {
                    if (count > 0) {
                        return repository.findHighestBidByLotId(lotId)
                                .map(Bid::getBidderId);
                    } else {
                        return Mono.just(0L);
                    }
                })
                .defaultIfEmpty(0L);
    }
}
