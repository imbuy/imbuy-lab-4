package imbuy.lot.infrastructure.persistence.repository;

import imbuy.lot.infrastructure.persistence.entity.LotFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotFileJpaRepository extends JpaRepository<LotFileEntity, Long> {
    List<LotFileEntity> findByLotId(Long lotId);
    boolean existsByLotIdAndFileId(Long lotId, Long fileId);
}

