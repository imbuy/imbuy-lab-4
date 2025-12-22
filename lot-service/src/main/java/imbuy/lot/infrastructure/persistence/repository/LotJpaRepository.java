package imbuy.lot.infrastructure.persistence.repository;

import imbuy.lot.domain.enums.LotStatus;
import imbuy.lot.infrastructure.persistence.entity.LotEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LotJpaRepository extends JpaRepository<LotEntity, Long> {

    List<LotEntity> findByStatus(LotStatus status, Pageable pageable);

    List<LotEntity> findByTitleContainingIgnoreCaseAndStatusAndCategoryIdAndOwnerId(
            String title,
            LotStatus status,
            Long categoryId,
            Long ownerId,
            Pageable pageable
    );
}
