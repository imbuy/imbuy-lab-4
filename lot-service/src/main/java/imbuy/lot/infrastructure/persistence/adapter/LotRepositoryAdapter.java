package imbuy.lot.infrastructure.persistence.adapter;

import imbuy.lot.application.port.out.LotRepositoryPort;
import imbuy.lot.domain.model.Lot;
import imbuy.lot.domain.enums.LotStatus;
import imbuy.lot.infrastructure.persistence.mapper.LotPersistenceMapper;
import imbuy.lot.infrastructure.persistence.repository.LotJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LotRepositoryAdapter implements LotRepositoryPort {

    private final LotJpaRepository repository;

    @Override
    public Lot save(Lot lot) {
        return LotPersistenceMapper.toDomain(
                repository.save(LotPersistenceMapper.toEntity(lot))
        );
    }

    @Override
    public Optional<Lot> findById(Long id) {
        return repository.findById(id)
                .map(LotPersistenceMapper::toDomain);
    }

    @Override
    public List<Lot> findByStatus(LotStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable)
                .stream()
                .map(LotPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Lot> findByFilters(
            String title,
            LotStatus status,
            Long categoryId,
            Long ownerId,
            Pageable pageable
    ) {
        return repository
                .findByTitleContainingIgnoreCaseAndStatusAndCategoryIdAndOwnerId(
                        title, status, categoryId, ownerId, pageable
                )
                .stream()
                .map(LotPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Lot lot) {
        repository.deleteById(lot.getId());
    }
}
