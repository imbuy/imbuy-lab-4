package imbuy.lot.application.port.in;

import imbuy.lot.application.dto.CreateLotDto;
import imbuy.lot.application.dto.LotDto;
import imbuy.lot.application.dto.LotFilterDto;
import imbuy.lot.application.dto.UpdateLotDto;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface LotUseCase {

    List<LotDto> getLots(LotFilterDto filter, Pageable pageable);

    LotDto getLotById(Long id);

    LotDto createLot(CreateLotDto dto, Long userId);

    LotDto approveLot(Long id, Long userId);

    LotDto cancelLot(Long id, Long userId, String reason);

    LotDto updateLot(Long id, UpdateLotDto dto, Long userId);

    void deleteLot(Long id, Long userId);
}
