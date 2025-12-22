package imbuy.lot.application.mapper;

import imbuy.lot.application.dto.LotDto;
import imbuy.lot.domain.model.Lot;

public class LotMapper {
    private LotMapper() {
    }

    public static LotDto toDto(Lot lot) {
        return new LotDto(lot.getId(), lot.getTitle(), lot.getDescription(), lot.getStartPrice(), lot.getCurrentPrice(), lot.getBidStep(), lot.getOwnerId(), null, // owner_username — обогащается отдельно
                lot.getCategoryId(),
                null, // category_name
                lot.getStatus(), lot.getStartDate(), lot.getEndDate(), lot.getWinnerId(),
                null // winner_username
        );
    }
}
