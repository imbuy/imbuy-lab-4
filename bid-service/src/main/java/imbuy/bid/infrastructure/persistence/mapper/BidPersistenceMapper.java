package imbuy.bid.infrastructure.persistence.mapper;

import imbuy.bid.domain.model.Bid;
import imbuy.bid.infrastructure.persistence.entity.BidEntity;
import org.springframework.stereotype.Component;

@Component
public class BidPersistenceMapper {

    public Bid toDomain(BidEntity e) {
        return Bid.builder()
                .id(e.getId())
                .lotId(e.getLotId())
                .bidderId(e.getBidderId())
                .amount(e.getAmount())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public BidEntity toEntity(Bid d) {
        return BidEntity.builder()
                .id(d.getId())
                .lotId(d.getLotId())
                .bidderId(d.getBidderId())
                .amount(d.getAmount())
                .createdAt(d.getCreatedAt())
                .build();
    }
}

