package imbuy.lot.infrastructure.persistence.adapter;

import imbuy.lot.application.port.out.LotFileRepositoryPort;
import imbuy.lot.domain.model.LotFile;
import imbuy.lot.infrastructure.persistence.mapper.LotFilePersistenceMapper;
import imbuy.lot.infrastructure.persistence.repository.LotFileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LotFileRepositoryAdapter implements LotFileRepositoryPort {

    private final LotFileJpaRepository repository;

    @Override
    public LotFile save(LotFile lotFile) {
        return LotFilePersistenceMapper.toDomain(
                repository.save(LotFilePersistenceMapper.toEntity(lotFile))
        );
    }

    @Override
    public List<LotFile> findByLotId(Long lotId) {
        return repository.findByLotId(lotId)
                .stream()
                .map(LotFilePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByLotIdAndFileId(Long lotId, Long fileId) {
        return repository.existsByLotIdAndFileId(lotId, fileId);
    }
}

