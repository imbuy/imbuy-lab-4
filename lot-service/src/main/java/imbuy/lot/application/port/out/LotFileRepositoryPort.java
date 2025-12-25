package imbuy.lot.application.port.out;

import imbuy.lot.domain.model.LotFile;

import java.util.List;

public interface LotFileRepositoryPort {
    LotFile save(LotFile lotFile);
    List<LotFile> findByLotId(Long lotId);
    boolean existsByLotIdAndFileId(Long lotId, Long fileId);
}

