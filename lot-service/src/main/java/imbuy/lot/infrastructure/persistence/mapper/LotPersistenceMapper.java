package imbuy.lot.infrastructure.persistence.mapper;

import imbuy.lot.domain.model.Lot;
import imbuy.lot.infrastructure.persistence.entity.LotEntity;

public final class LotPersistenceMapper {

    private LotPersistenceMapper() {}

    public static Lot toDomain(LotEntity e) {
        return Lot.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .startPrice(e.getStartPrice())
                .currentPrice(e.getCurrentPrice())
                .bidStep(e.getBidStep())
                .ownerId(e.getOwnerId())
                .categoryId(e.getCategoryId())
                .winnerId(e.getWinnerId())
                .status(e.getStatus())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public static LotEntity toEntity(Lot d) {
        return LotEntity.builder()
                .id(d.getId())
                .title(d.getTitle())
                .description(d.getDescription())
                .startPrice(d.getStartPrice())
                .currentPrice(d.getCurrentPrice())
                .bidStep(d.getBidStep())
                .ownerId(d.getOwnerId())
                .categoryId(d.getCategoryId())
                .winnerId(d.getWinnerId())
                .status(d.getStatus())
                .startDate(d.getStartDate())
                .endDate(d.getEndDate())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
