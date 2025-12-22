package imbuy.bid.application.port.in;

import imbuy.bid.application.dto.BidDto;
import imbuy.bid.application.dto.CreateBidDto;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BidUseCase {

    Flux<BidDto> getBidsByLotId(Long lotId, Pageable pageable);

    Mono<BidDto> placeBid(Long lotId, CreateBidDto dto, Long userId);

    Mono<Long> getAuctionWinnerId(Long lotId);
}
