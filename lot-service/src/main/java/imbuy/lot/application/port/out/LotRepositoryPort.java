package imbuy.lot.application.port.out;

import imbuy.lot.domain.model.Lot;
import imbuy.lot.domain.enums.LotStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LotRepositoryPort {

    Lot save(Lot lot);

    Optional<Lot> findById(Long id);

    List<Lot> findByStatus(LotStatus status, Pageable pageable);

    List<Lot> findByFilters(
            String title,
            LotStatus status,
            Long categoryId,
            Long ownerId,
            Pageable pageable
    );

    void delete(Lot lot);
}
